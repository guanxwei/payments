package org.wgx.payments.job;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.RefundRequest;
import org.wgx.payments.client.api.io.RefundResponse;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.dao.service.PaymentsDAOService;
import org.wgx.payments.execution.PaymentProcessorManager;
import org.wgx.payments.execution.SimplePaymentProcessor;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.model.FastSearchTableItemStatus;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Back-end job to refund duplicated charge requests.
 *
 */
@Setter
@Slf4j
public class AutoRefundJob {

    @Resource
    private FastSearchTableDAO fastSearchTableDAO;

    @Resource
    private PaymentResponseDAO paymentResponseDAO;

    @Resource(name = "paymentProcessorManager")
    private PaymentProcessorManager paymentProcessorManager;

    @Resource
    private PaymentsDAOService paymentsDAOService;

    @Resource
    private PaymentRequestDAO paymentRequestDAO;

    private int delay = 60;

    private int period = 600;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    /**
     * Initiate the job.
     */
    public void init() {
        log.info("This is a job host!");
        AutoRefundRunner job = new AutoRefundRunner();
        executor.scheduleAtFixedRate(job, delay, period, TimeUnit.SECONDS);
    }

    /**
     * Auto refund back-end runner.
     *
     */
    public class AutoRefundRunner implements Runnable {

        @Override
        public void run() {
            log.info("Auto refund job start to work at time [{}]", Timestamp.valueOf(LocalDateTime.now()));

            List<FastSearchTableItem> refunds = fastSearchTableDAO.list("refund", FastSearchTableItemStatus.PENDING.status());
            if (refunds == null || refunds.isEmpty()) {
                log.info("There is no request need to be auto refunded! Nice day!");
                return;
            }

            for (FastSearchTableItem refund : refunds) {
                postRefund(refund);
            }
            log.info("Auto refund job quit at time [{}]", Timestamp.valueOf(LocalDateTime.now()));
        }
    }

    private void postRefund(final FastSearchTableItem refund) {
        String transactionID = refund.getTransactionID();
        log.info("Process transaction id : [{}]", transactionID);
        PaymentResponse response = paymentResponseDAO.getPaymentResponseByTransactionID(transactionID);
        PaymentRequest request = paymentRequestDAO.getPaymentRequestByTransactionID(transactionID);
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setBusiness(response.getBusiness());
        refundRequest.setCustomerID(response.getCustomerID());
        refundRequest.setPaymentOperationType(PaymentOperation.REFUND.operationType());
        refundRequest.setReferenceID(response.getReferenceID());
        refundRequest.setRefundAmount(response.getAcknowledgedAmount());
        refundRequest.setTransactionID(transactionID);
        refundRequest.setChannel(request.getChannel());
        Map<String, String> specialTags = new HashMap<>();
        specialTags.put("channel", "autorefund");
        refundRequest.setSpecialTags(specialTags);

        SimplePaymentProcessor processor = (SimplePaymentProcessor) paymentProcessorManager.retrievePaymentProcessor(response.getPaymentMethod());
        processor.getLocalResponse().set(response);
        RefundResponse refundResponse = (RefundResponse) processor.processRequest(refundRequest);
        if (refundResponse.getResponseCode() == ResponseStatus.INTERNAL_ERROR_CODE) {
            log.warn("Fail to auto refund transaction : [{}], will try again later!", transactionID);
        } else {
            response.setLastUpdateTime(Timestamp.valueOf(LocalDateTime.now()));
            paymentsDAOService.updateAutoRefundRecords(refund, response);
            log.info("Auto refund for transaction [{}] completed.", transactionID);
        }
    }
}
