package org.wgx.payments.job;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.wgx.payments.builder.ActionRecordBuilder;
import org.wgx.payments.builder.PaymentResponseBuilder;
import org.wgx.payments.callback.Callback;
import org.wgx.payments.callback.CallbackDetail;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.io.CallbackMetaInfo;
import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.dao.service.PaymentsDAOService;
import org.wgx.payments.facade.WechatFacade;
import org.wgx.payments.model.ActionRecord;
import org.wgx.payments.model.ActionRecord.ErrorCode;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.AccountUtils;
import org.wgx.payments.utils.WechatConstants;
import org.wgx.payments.utils.WechatSignatureHelper;
import org.wgx.payments.utils.WechatUtils;
import org.wgx.payments.utils.XMLUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Wechat job to query Wechat refund transaction status.
 *
 */
@Slf4j
@Setter
public class WechatQueryRefundJob {

    private static final int LIMIT = 10;

    private int delay = 60;

    private int period = 600;

    @Resource
    private PaymentRequestDAO paymentRequestDAO;

    @Resource
    private PaymentResponseDAO paymentResponseDAO;

    @Resource(name = "wechatFacade")
    private WechatFacade facade;

    @Resource
    private ActionRecordDAO actionRecordDAO;

    @Resource
    private Callback callback;

    @Resource
    private PaymentsDAOService paymentsDAOService;

    @Resource @Setter
    private AccountFactory accountFactory;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    /**
     * Initiate the job.
     */
    public void init() {
        log.info("This is a job host!");
        QueryRefundJobRunner job = new QueryRefundJobRunner();
        executor.scheduleAtFixedRate(job, delay, period, TimeUnit.SECONDS);
    }

    /**
     * Job runner to query pending Wechat refund requests and update this requests.
     *
     */
    public class QueryRefundJobRunner implements Runnable {

        @Override
        public void run() {
            log.info("Wechat query refund job start to work at time [{}]", Timestamp.valueOf(LocalDateTime.now()));

            List<PaymentRequest> pendingRefundRequests = paymentRequestDAO.getPendingPaymentRequestList(PaymentRequestStatus.PENDING_ON_QUERY.status(),
                    PaymentMethod.WECHAT.paymentMethodName(), PaymentOperation.REFUND.operationType(), LIMIT);
            if (pendingRefundRequests == null || pendingRefundRequests.isEmpty()) {
                log.info("Currently there is no pending on query Wechat refund requests, nice day!");
                return;
            }

            pendingRefundRequests.forEach(request -> {
                query(request);
            });

            log.info("Wechat query refund job complete task at time [{}]", Timestamp.valueOf(LocalDateTime.now()));
        }
    }

    private void query(final PaymentRequest request) {
        log.info("Processing request [{}]", request.getId());
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        String accountName = request.getUrl();
        AccountUtils.set(accountFactory.getAccount(accountName));
        String queryRefundXMLString = WechatUtils.buildQueryRefundXMLString(request.getTransactionID(), accountFactory.getAccount(accountName).getPrivateKey(),
                accountFactory.getAccount(accountName).getPublicKey());

        Pair<String, String> pair = Pair.of(WechatConstants.QUERY_REFUND_API, queryRefundXMLString);
        String response = facade.call(pair);
        Pair<Integer, String> parsedWechatResponse = parseResponse(response, request, now);
        if (parsedWechatResponse.getLeft() < 0) {
            return;
        }

        if (!succeed(parsedWechatResponse, request, now)) {
            return;
        }

        String externalTransactionID = parsedWechatResponse.getRight();
        Pair<Boolean, String> callbackResult = callback(request, parsedWechatResponse.getLeft());
        if (!callbackResult.getLeft()) {
            record(String.format("Call back call failed for due to [%s]", callbackResult.getRight()), request.getTransactionID(),
                    ErrorCode.CALLBACK_FAIL.code());
            return;
        }

        update(request, now, externalTransactionID, response, parsedWechatResponse.getLeft());
    }

    private int deduceStatus(final String refundStatusCode) {
        if ("SUCCESS".equals(refundStatusCode)) {
            return PaymentResponseStatus.SUCCESS.status();
        } else if ("REFUNDCLOSE".equals(refundStatusCode) || "CHANGE".equals(refundStatusCode)) {
            return PaymentResponseStatus.FAIL.status();
        } else if ("PROCESSING".equals(refundStatusCode)) {
            return PaymentResponseStatus.PENDING.status();
        } else {
            return PaymentResponseStatus.UNVALID.status();
        }
    }

    private void record(final String message, final String transactionID, final int code) {
        ActionRecord record = ActionRecordBuilder.builder()
                .message(message)
                .errorCode(code)
                .time(Timestamp.valueOf(LocalDateTime.now()))
                .transactionID(transactionID)
                .build();
        actionRecordDAO.record(record);
    }

    private Pair<Integer, String> parseResponse(final String response, final PaymentRequest request, final Timestamp now) {
        Pair<Integer, String> result = Pair.of(-1, null);
        Map<String, Object> parameters = null;
        boolean verifyResult = false;
        try {
            parameters = XMLUtils.getMapFromXML(response);
            String accountName = request.getUrl();
            verifyResult = WechatSignatureHelper.signVerifyWithKey(parameters, accountFactory.getAccount(accountName).getPublicKey());
        } catch (Exception e) {
            log.warn("Wechat XML String response processing error.", e);
            return result;
        }

        if (!verifyResult) {
            record("Wechat query refund response verification failed",
                    request.getTransactionID(), ErrorCode.VALIDATION_FAIL.code());
            return result;
        }

        String returnCode = (String) parameters.get("return_code");
        if ("SUCCESS".equals(returnCode)) {
            String refundStatusCode = (String) parameters.get("refund_status_0");
            result = Pair.of(deduceStatus(refundStatusCode), (String) parameters.get("refund_id_0"));
        } else {
            record("Wechat fail to receive our query refund request, will try again later",
                    request.getTransactionID(), ErrorCode.PAYMENT_FAIL.code());
        }

        return result;
    }

    private boolean succeed(final Pair<Integer, String> result, final PaymentRequest request, final Timestamp now) {
        if (result.getLeft() == PaymentResponseStatus.PENDING.status()) {
            request.setLastUpdateTime(now);
            paymentRequestDAO.update(request);
            return false;
        } else if (result.getLeft() == PaymentResponseStatus.UNVALID.status()) {
            request.setStatus(PaymentRequestStatus.PENDING_ON_RETRY.status());
            request.setLastUpdateTime(now);
            paymentRequestDAO.update(request);
            return false;
        }
        return true;
    }

    private Pair<Boolean, String> callback(final PaymentRequest request, final int status) {
        Pair<Boolean, String> result = Pair.of(true, null);
        if (request != null && request.getCallBackMetaInfo() != null && !request.getCallBackMetaInfo().equals("null")
                && !request.getCallBackMetaInfo().equals(StringUtils.EMPTY)) {
            CallbackMetaInfo info = Jackson.parse(request.getCallBackMetaInfo(), CallbackMetaInfo.class);
            if (info.getParameters() == null) {
                info.setParameters(new HashMap<>());
            }
            info.getParameters().put("transactionID", request.getTransactionID());
            info.getParameters().put("status", String.valueOf(status));
            info.getParameters().put("paymentMethod", PaymentMethod.WECHAT.paymentMethodName());
            CallbackDetail callbackDetail = callback.call(info);
            result = Pair.of(callbackDetail.isSucceed(), callbackDetail.getError());
        }
        return result;
    }

    private void update(final PaymentRequest request, final Timestamp now, final String externalTransactionID,
            final String rawResponse, final int status) {
        request.setLastUpdateTime(now);
        request.setStatus(PaymentRequestStatus.PAID.status());
        PaymentResponse paymentResponse = PaymentResponseBuilder.builder()
                .business(request.getBusiness())
                .acknowledgedAmount(request.getRequestedAmount())
                .createTime(now)
                .customerID(request.getCustomerID())
                .externalTransactionID(externalTransactionID)
                .lastUpdateTime(now)
                .operationType(PaymentOperation.REFUND.operationType())
                .paymentMethod(request.getPaymentMethod())
                .rawResponse(rawResponse)
                .referenceID(request.getReferenceID())
                .status(status)
                .transactionID(request.getTransactionID())
                .build();
        List<PaymentResponse> responses = new LinkedList<>();
        List<PaymentRequest> requests = new LinkedList<>();
        responses.add(paymentResponse);
        requests.add(request);
        paymentsDAOService.updateRequestAndResponse(requests, responses);
    }
}
