package org.wgx.payments.stream.activities.create;

import java.sql.Timestamp;

import org.stream.core.component.Activity;
import org.stream.core.component.ActivityResult;
import org.stream.core.execution.WorkFlowContext;
import org.stream.core.helper.Jackson;
import org.stream.core.resource.Resource;
import org.wgx.payments.builder.PaymentRequestBuilder;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.stream.config.WellknownResourceReferences;
import org.wgx.payments.utils.AmountUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Activity used to initiate payment request.
 * @author weigu
 *
 */
@Slf4j
public class InitiatePaymentRequest extends Activity {

    @javax.annotation.Resource
    private PaymentRequestDAO paymentRequestDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityResult act() {
        Resource primary = WorkFlowContext.getPrimary();
        CreatePaymentRequest createPaymentRequest = primary.resolveValue(CreatePaymentRequest.class);
        PaymentRequest paymentRequest = PaymentRequestBuilder.builder()
                .business(createPaymentRequest.getBusiness())
                .callBackMetaInfo(Jackson.json(createPaymentRequest.getCallbackInfo()))
                .channel(createPaymentRequest.getChannel())
                .createTime(new Timestamp(System.currentTimeMillis()))
                .customerID(createPaymentRequest.getCustomerID())
                .lastUpdateTime(new Timestamp(System.currentTimeMillis()))
                .paymentMethod(Jackson.json(createPaymentRequest.getPaymentMethod()))
                .paymentOperationType(createPaymentRequest.getPaymentOperationType())
                .referenceIDList(Jackson.json(createPaymentRequest.getReferences().keySet()))
                .requestedAmount(AmountUtils.total(createPaymentRequest.getReferences().values()).toString())
                .status(PaymentRequestStatus.PENDING.status())
                .build();

        paymentRequestDAO.save(paymentRequest);
        Resource resource = Resource.builder()
                .resourceReference(WellknownResourceReferences.PAYMENT_REQUEST)
                .value(paymentRequest)
                .build();
        WorkFlowContext.attachResource(resource);
        log.info("Payment request [{}] initiated", Jackson.json(paymentRequest));
        return ActivityResult.SUCCESS;
    }

}
