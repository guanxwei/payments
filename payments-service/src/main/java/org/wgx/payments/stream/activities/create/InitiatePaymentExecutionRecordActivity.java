package org.wgx.payments.stream.activities.create;

import java.util.LinkedList;
import java.util.List;

import org.stream.core.component.Activity;
import org.stream.core.component.ActivityResult;
import org.stream.core.execution.WorkFlowContext;
import org.stream.core.resource.Resource;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.dao.PaymentExecutionRecordDAO;
import org.wgx.payments.model.PaymentExecutionRecord;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.stream.config.WellknownResourceReferences;

/**
 * Initiate payment execution record for the references.
 * 
 * @author weiguanxiong
 *
 */
public class InitiatePaymentExecutionRecordActivity extends Activity {

    @javax.annotation.Resource
    private PaymentExecutionRecordDAO paymentExecutionRecordDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityResult act() {
        Resource primary = WorkFlowContext.getPrimary();
        CreatePaymentRequest createPaymentRequest = primary.resolveValue(CreatePaymentRequest.class);
        List<PaymentExecutionRecord> paymentExecutionRecords = new LinkedList<>();
        fill(paymentExecutionRecords, createPaymentRequest);
        paymentExecutionRecordDAO.save(paymentExecutionRecords);

        return ActivityResult.SUCCESS;
    }

    private void fill(final List<PaymentExecutionRecord> paymentExecutionRecords,
            final CreatePaymentRequest createPaymentRequest) {
        Resource resource = WorkFlowContext.resolveResource(WellknownResourceReferences.PAYMENT_REQUEST);
        PaymentRequest paymentRequest = resource.resolveValue(PaymentRequest.class);
        createPaymentRequest.getReferences().forEach((key, value) -> {
            PaymentExecutionRecord paymentExecutionRecord = new PaymentExecutionRecord();
            paymentExecutionRecord.setExecuteTime(System.currentTimeMillis());
            paymentExecutionRecord.setPaymentRequestID(paymentRequest.getId());
            paymentExecutionRecord.setReferenceID(key);
            paymentExecutionRecord.setRequestAmount(value);
            paymentExecutionRecord.setStatus(0);
            paymentExecutionRecords.add(paymentExecutionRecord);
        });
    }

}
