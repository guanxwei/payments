package org.wgx.payments.stream.activities.create;

import java.util.LinkedList;
import java.util.List;

import javax.validation.ValidationException;

import org.springframework.stereotype.Component;
import org.stream.core.component.Activity;
import org.stream.core.component.ActivityResult;
import org.stream.core.execution.WorkFlowContext;
import org.stream.core.resource.Resource;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.stream.config.WellknownResourceReferences;

import lombok.extern.slf4j.Slf4j;

/**
 * Stream work-flow based activity to parse payment method list
 * from the request.
 * @author weiguanxiong
 *
 */
@Slf4j
@Component
public class ParsePaymentMethodListActivity extends Activity {

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityResult act() {
        Resource primary = WorkFlowContext.getPrimary();
        CreatePaymentRequest createPaymentRequest = primary.resolveValue(CreatePaymentRequest.class);
        List<PaymentMethod> paymentMethods = new LinkedList<>();
        createPaymentRequest.getPaymentMethod().forEach(code -> {
            PaymentMethod paymentMethod = PaymentMethod.fromCode(code);
            if (paymentMethod == null) {
                log.error("Unsupported payment method code [{}]", code);
                throw new ValidationException(String.format("Unsupported payment method code [%d]", code));
            }
            paymentMethods.add(paymentMethod);
            log.info("Payment [{}] added to the processing list", paymentMethod.paymentMethodName());
        });

        Resource resource = Resource.builder()
                .resourceReference(WellknownResourceReferences.PAYMENT_METHOD_LIST)
                .value(paymentMethods)
                .build();

        WorkFlowContext.attachResource(resource);
        return ActivityResult.SUCCESS;
    }

}
