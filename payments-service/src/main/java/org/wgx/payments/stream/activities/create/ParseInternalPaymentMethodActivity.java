package org.wgx.payments.stream.activities.create;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.stream.core.component.Activity;
import org.stream.core.component.ActivityResult;
import org.stream.core.execution.WorkFlowContext;
import org.stream.core.resource.Resource;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.exception.PaymentsException;
import org.wgx.payments.stream.config.WellknownResourceReferences;

import lombok.extern.slf4j.Slf4j;

/**
 * Stream activity used to parse internal used only payment method
 * from the pre parsed payment method list.
 * @author weigu
 *
 */
@Component
@Slf4j
public class ParseInternalPaymentMethodActivity extends Activity {

    /**
     * {@inheritDoc}
     */
    @Override
    public ActivityResult act() {

        Resource resource = WorkFlowContext.resolveResource(WellknownResourceReferences.PAYMENT_METHOD_LIST);
        @SuppressWarnings("unchecked")
        List<PaymentMethod> paymentMethods = resource.resolveValue(List.class);
        long count = paymentMethods.parallelStream()
                .filter(PaymentMethod::isInternal)
                .count();
        if (count > 1L) {
            throw new PaymentsException(400, "Request not supported, more then two kinds of internal payment methods are presented.");
        }
        Optional<PaymentMethod> internalPaymentMethodOption = paymentMethods.parallelStream()
                .filter(PaymentMethod::isInternal)
                .findFirst();
        if (internalPaymentMethodOption.isPresent()) {
            PaymentMethod paymentMethod = internalPaymentMethodOption.get();
            Resource internalPaymentMethodResource = Resource.builder()
                    .resourceReference(WellknownResourceReferences.INTERNAL_PAYMENT_METHOD)
                    .value(paymentMethod)
                    .build();
            WorkFlowContext.attachResource(internalPaymentMethodResource);
            log.info("Internal payment method [{}] parsed for the request", paymentMethod.paymentMethodName());
        }
        return ActivityResult.SUCCESS;
    }

}
