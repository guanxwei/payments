package org.wgx.payments.stream.activities.create;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.stream.core.component.Activity;
import org.stream.core.component.ActivityResult;
import org.stream.core.execution.WorkFlowContext;
import org.stream.core.resource.Resource;
import org.wgx.payments.builder.PaymentRequestBuilder;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.stream.config.WellknownResourceReferences;
import org.wgx.payments.tools.Jackson;

import lombok.extern.slf4j.Slf4j;

/**
 * Activity used to initiate payment request.
 * @author weigu
 *
 */
@Slf4j
public class InitiatePaymentRequestActivity extends Activity {

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
                .id(paymentRequestDAO.allocateID())
                .lastUpdateTime(new Timestamp(System.currentTimeMillis()))
                .parentRequestID(0)
                .paymentMethod(PaymentMethod.ALIPAY.paymentMethodCode())
                .paymentOperationType(createPaymentRequest.getPaymentOperationType())
                .referenceID(null)
                .requestedAmount(createPaymentRequest.getReferences().values()
                        .stream()
                        .map(BigDecimal::new)
                        .reduce(BigDecimal::add)
                        .get()
                        .setScale(2)
                        .toString()
                        )
                .status(PaymentRequestStatus.PENDING.status())
                .build();
        log.info("Initiate primary payment request [{}] for the client request", paymentRequest.toString());

        Resource primaryRequest = Resource.builder()
                .value(paymentRequest)
                .resourceReference(WellknownResourceReferences.PAYMENT_REQUEST)
                .build();
        WorkFlowContext.attachResource(primaryRequest);
        return ActivityResult.SUCCESS;
    }

}
