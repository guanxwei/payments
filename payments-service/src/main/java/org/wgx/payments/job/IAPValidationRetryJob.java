package org.wgx.payments.job;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.wgx.payments.builder.PaymentResponseBuilder;
import org.wgx.payments.callback.Callback;
import org.wgx.payments.callback.CallbackDetail;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.io.CallbackMetaInfo;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.clients.RedisClient;
import org.wgx.payments.dao.ActionRecordDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.dao.service.PaymentsDAOService;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.model.PaymentResponse;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.validator.IAPValidator;
import org.wgx.payments.validator.Validator;

import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * IAP validation retry job. Since Apple servers are located in foreign regions, it is very commonly that we
 * will fail to communicate with Apple server to validate received receipt, so we have to set up an back end
 * retry job to retry validation when the original request failed(fail to communicate with Apple servers.)
 *
 */
@Slf4j
@Data
public class IAPValidationRetryJob {

    private int delay = 10;

    private int period = 30;

    @Resource
    private PaymentRequestDAO paymentRequestDAO;

    @Resource
    private PaymentResponseDAO paymentResponseDAO;

    @Resource
    private ActionRecordDAO actionRecordDAO;

    @Resource
    private Callback callback;

    @Resource
    private PaymentsDAOService paymentsDAOService;

    @Resource(name = "IAPValidator")
    private Validator<CreateOrUpdatePaymentResponseRequest> iapValidator;

    @Resource
    @Setter
    private RedisClient redisService;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    /**
     * Initiate the job.
     */
    public void init() {
        log.info("This is a job host!");
        IAPValidationRetryRunner job = new IAPValidationRetryRunner();
        executor.scheduleAtFixedRate(job, delay, period, TimeUnit.SECONDS);
    }

    /**
     * IAP validation retry job.
     *
     */
    public class IAPValidationRetryRunner implements Runnable {
        @Override
        public void run() {
            log.info("IAP auto retry job begin to work at time :", Timestamp.valueOf(LocalDateTime.now()));
            List<String> transactions = redisService.getList(IAPValidator.IAP_VALIDATION_FAILED_LIST, 20);
            for (String transaction : transactions) {
                process(transaction);
            }
        }
    }

    private void process(final String transaction) {
        String content = redisService.get(IAPValidator.IAP_VALIDATION_FAILED_TRANSACTION_PREFIX + transaction);

        boolean succeed = validate(transaction, content);
        if (!succeed) {
            return;
        }

        // Fail to push the call-back message to the clients, we will try again later.
        PaymentRequest paymentRequest = paymentRequestDAO.getPaymentRequestByTransactionID(transaction);
        succeed = callback(paymentRequest, succeed, transaction);

        if (!succeed) {
            return;
        }

        update(paymentRequest, content, succeed);
    }

    private boolean validate(final String transaction, final String content) {
        CreateOrUpdatePaymentResponseRequest request = Jackson.parse(content,
                CreateOrUpdatePaymentResponseRequest.class);
        try {
            return iapValidator.validate(request);
        } catch (Exception e) {
            log.info("Fail again, we will retry later");
            return false;
        }
    }

    private boolean callback(final PaymentRequest paymentRequest, final boolean passedValidation, final String transaction) {
        if (paymentRequest.getCallBackMetaInfo() != null && !paymentRequest.getCallBackMetaInfo().equals("null")
                && !paymentRequest.getCallBackMetaInfo().equals(StringUtils.EMPTY)) {
            CallbackMetaInfo callbackMetaInfo = Jackson.parse(paymentRequest.getCallBackMetaInfo(), CallbackMetaInfo.class);
            if (callbackMetaInfo.getParameters() == null) {
                callbackMetaInfo.setParameters(new HashMap<>());
            }
            callbackMetaInfo.getParameters().put("status", String.valueOf(passedValidation ? PaymentResponseStatus.SUCCESS.status()
                : PaymentResponseStatus.FAIL.status()));
            callbackMetaInfo.getParameters().put("transactionID", transaction);
            callbackMetaInfo.getParameters().put("paymentMethod", PaymentMethod.IAP.paymentMethodName());
            CallbackDetail callbackDetail = callback.call(callbackMetaInfo);
            return callbackDetail.isSucceed();
        }
        return true;
    }

    private void update(final PaymentRequest paymentRequest, final String content, final boolean succeed) {
        paymentRequest.setStatus(PaymentRequestStatus.PAID.status());
        paymentRequest.setLastUpdateTime(Timestamp.valueOf(LocalDateTime.now()));
        PaymentResponse paymentResponse = PaymentResponseBuilder.builder()
                .acknowledgedAmount(paymentRequest.getRequestedAmount())
                .business(paymentRequest.getBusiness())
                .createTime(Timestamp.valueOf(LocalDateTime.now()))
                .customerID(paymentRequest.getCustomerID())
                .externalTransactionID(StringUtils.EMPTY)
                .lastUpdateTime(Timestamp.valueOf(LocalDateTime.now()))
                .operationType(PaymentOperation.CHARGE.operationType())
                .paymentMethod(paymentRequest.getPaymentMethod())
                .rawResponse(content)
                .referenceID(paymentRequest.getReferenceID())
                .status(succeed ? PaymentResponseStatus.SUCCESS.status()
                    : PaymentResponseStatus.FAIL.status())
                .transactionID(paymentRequest.getTransactionID())
                .build();
        List<PaymentRequest> requests = new LinkedList<>();
        List<PaymentResponse> responses = new LinkedList<>();
        requests.add(paymentRequest);
        responses.add(paymentResponse);
        paymentsDAOService.updateRequestAndResponse(requests, responses);
        redisService.sremove(IAPValidator.IAP_VALIDATION_FAILED_LIST, paymentRequest.getTransactionID());
    }
}
