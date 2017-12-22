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
import org.wgx.payments.client.api.io.RescindRequest;
import org.wgx.payments.client.api.io.RescindResponse;
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
import org.wgx.payments.tools.Jackson;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Back-end auto rescind job.
 *
 */
@Slf4j
@Setter
public class AutoRescindJob {

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
        RescindRunner job = new RescindRunner();
        executor.scheduleAtFixedRate(job, delay, period, TimeUnit.SECONDS);
    }

    /**
     * Back-end rescind runner.
     *
     */
    public class RescindRunner implements Runnable {

        @Override
        public void run() {
            log.info("Auto rescind job start to work at time " + Timestamp.valueOf(LocalDateTime.now()));

            List<FastSearchTableItem> rescinds = fastSearchTableDAO.list("rescind", FastSearchTableItemStatus.PENDING.status());
            if (rescinds == null || rescinds.isEmpty()) {
                log.info("There is no request need to be auto rescinded! Nice day!");
                return;
            }

            for (FastSearchTableItem rescindItem : rescinds) {
                postRescind(rescindItem);
            }
        }
    }

    private void postRescind(final FastSearchTableItem rescindItem) {
        String transactionID = rescindItem.getTransactionID();
        log.info("Process transaction id : [{}]", transactionID);
        PaymentResponse response = paymentResponseDAO.getPaymentResponseByTransactionID(transactionID);
        log.info("Found sign response [{}]", Jackson.json(response));
        PaymentRequest request = paymentRequestDAO.getPaymentRequestByTransactionID(transactionID);
        log.info("Found sign request [{}]", Jackson.json(request));
        RescindRequest rescindRequest = new RescindRequest();
        rescindRequest.setBusiness(response.getBusiness());
        rescindRequest.setCustomerID(response.getCustomerID());
        rescindRequest.setPaymentOperationType(PaymentOperation.RESCIND.operationType());
        rescindRequest.setChannel(request.getChannel());
        Map<String, String> specialTags = new HashMap<>();
        specialTags.put("channel", "autounsign");
        rescindRequest.setSpecialTags(specialTags);
        SimplePaymentProcessor processor = (SimplePaymentProcessor) paymentProcessorManager.retrievePaymentProcessor(response.getPaymentMethod());
        processor.getLocalResponse().set(response);
        RescindResponse rescindResonse = (RescindResponse) processor.processRequest(rescindRequest);
        if (rescindResonse.getResponseCode() == ResponseStatus.INTERNAL_ERROR_CODE) {
            log.warn("Fail to auto rescind transaction : [{}], will try again later!", transactionID);
        } else {
            response.setLastUpdateTime(Timestamp.valueOf(LocalDateTime.now()));
            paymentsDAOService.updateAutoRefundRecords(rescindItem, response);
            log.info("Auto rescind for transaction [{}] completed.", transactionID);
        }
    }
}
