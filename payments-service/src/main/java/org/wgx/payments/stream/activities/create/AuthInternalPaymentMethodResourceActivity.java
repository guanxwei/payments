package org.wgx.payments.stream.activities.create;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.stream.core.component.Activity;
import org.stream.core.component.ActivityResult;
import org.stream.core.execution.WorkFlowContext;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.points.api.PointsService;
import org.wgx.payments.points.io.ConsumePointsRequest;
import org.wgx.payments.stream.config.WellknownResourceReferences;
import org.wgx.payments.utils.AmountUtils;

/**
 * Consume internal payment method resource if needed.
 * @author weigu
 *
 */
@Component
public class AuthInternalPaymentMethodResourceActivity extends Activity {

    @Resource
    private PointsService pointsService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityResult act() {
        org.stream.core.resource.Resource resource = WorkFlowContext.resolveResource(
                WellknownResourceReferences.INTERNAL_PAYMENT_METHOD);
        org.stream.core.resource.Resource primary = WorkFlowContext.getPrimary();
        CreatePaymentRequest createPaymentRequest = primary.resolveValue(CreatePaymentRequest.class);
        if (resource != null) {
            PaymentMethod paymentMethod = resource.resolveValue(PaymentMethod.class);
            return consume(paymentMethod, createPaymentRequest);
        }
        return ActivityResult.SUCCESS;
    }

    private ActivityResult consume(final PaymentMethod paymentMethod, final CreatePaymentRequest createPaymentRequest) {
        if (paymentMethod == PaymentMethod.POINTS) {
            ConsumePointsRequest consumePointsRequest = new ConsumePointsRequest();
            BigDecimal totalAmount = AmountUtils.total(createPaymentRequest.getReferences().values());
            consumePointsRequest.setAmount(totalAmount.setScale(2).toString());
            consumePointsRequest.setPartialTolarent(true);
            consumePointsRequest.setUserID(createPaymentRequest.getCustomerID());
            consumePointsRequest.setReason("支付申请扣费");
            consumePointsRequest.setRequestID(null);
        }
        return ActivityResult.SUCCESS;
    }

}
