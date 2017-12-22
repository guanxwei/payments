package org.wgx.payments.job;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.tuple.Pair;
import org.wgx.payments.builder.ActionRecordBuilder;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.facade.WechatFacade;
import org.wgx.payments.model.ActionRecord;
import org.wgx.payments.model.ActionRecord.ErrorCode;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.utils.AccountUtils;
import org.wgx.payments.utils.WechatConstants;
import org.wgx.payments.utils.WechatSignatureHelper;
import org.wgx.payments.utils.WechatUtils;
import org.wgx.payments.utils.XMLUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Wechat processor's refund retry job.
 *
 */
@Slf4j
@Setter
public class WechatRefundRetryJob {

    private static final int LIMIT = 10;

    private int delay = 60;

    private int period = 600;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Resource
    private PaymentRequestDAO paymentRequestDAO;

    @Resource(name = "wechatFacade")
    private WechatFacade facade;

    @Resource
    private ActionRecordDAO actionRecordDAO;

    @Resource @Setter
    private AccountFactory accountFactory;

    /**
     * Job initiation method.
     */
    public void init() {
        log.info("This is a job host!");
        RefundRetryJobRunner job = new RefundRetryJobRunner();
        executor.scheduleAtFixedRate(job, delay, period, TimeUnit.SECONDS);
    }

    /**
     * Wechat processor's refund retry job's worker.
     *
     */
    public class RefundRetryJobRunner implements Runnable {

        @Override
        public void run() {
            log.info("Wechat refund retry job start to work at time [{}]", Timestamp.valueOf(LocalDateTime.now()));

            List<PaymentRequest> pendingOnRetryRequests = paymentRequestDAO.getPendingPaymentRequestList(PaymentRequestStatus.PENDING_ON_RETRY.status(),
                    PaymentMethod.WECHAT.paymentMethodName(), PaymentOperation.REFUND.operationType(), LIMIT);
            if (pendingOnRetryRequests == null || pendingOnRetryRequests.isEmpty()) {
                log.info("Currently there is no pending on retry refund Wechat requests, nice day!");
                return;
            }

            pendingOnRetryRequests.forEach(request -> {
                PaymentRequest paidRequest = paymentRequestDAO.getPaymentRequestByTransactionID(request.getParentID());
                String accountName = request.getUrl();
                AccountUtils.set(accountFactory.getAccount(accountName));
                String xmlString = WechatUtils.buildRefundXMLString(request.getUrl(), request.getParentID(),
                        request.getTransactionID(), paidRequest.getRequestedAmount(), request.getRequestedAmount(),
                        accountFactory.getAccount(accountName).getPublicKey());
                Pair<String, String> pair = Pair.of(WechatConstants.REFUND_API, xmlString);
                String wechatResponse = facade.call(pair);
                log.info("Receive wechat response :" + wechatResponse);
                Map<String, Object> responseMap = null;
                try {
                    responseMap = XMLUtils.getMapFromXML(wechatResponse);
                    boolean verify = WechatSignatureHelper.signVerifyWithKey(responseMap, accountFactory.getAccount(accountName).getPublicKey());
                    if (verify) {
                        // Refund failed.
                        if ("FAIL".equals(responseMap.get("return_code")) || "FAIL".equals(responseMap.get("result_code"))) {
                            record(String.format("Wechat refund failed due to [%s]", responseMap.get("err_code_des")), request.getTransactionID(),
                                    ErrorCode.PAYMENT_FAIL.code());
                        } else {
                            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                            request.setLastUpdateTime(now);
                            request.setStatus(PaymentRequestStatus.PENDING_ON_QUERY.status());
                            paymentRequestDAO.update(request);
                            log.info("Update payment request [%s] to status pending", request.getTransactionID());
                        }
                    } else {
                        log.warn("Wechat response verification failed, something wrong happened. Raw response : [{}]", wechatResponse);
                        record(String.format("Wechat response verification failed, something wrong happened. Raw response : [%s]",
                                wechatResponse), request.getTransactionID(), ErrorCode.VALIDATION_FAIL.code());
                    }
                } catch (Exception e) {
                    log.error("Fail to parse wechar refund response.", e);
                    return;
                }
            });

            log.info("Wechat query refund job complete task at time [{}]", Timestamp.valueOf(LocalDateTime.now()));
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
    }
}
