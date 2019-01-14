package org.wgx.payments.providers;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.wgx.payments.client.api.Service;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseResponse;
import org.wgx.payments.execution.PaymentProcessor;
import org.wgx.payments.execution.PaymentProcessorManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment service to Create or Update PaymentResponse.
 *
 */
@Component(value = "createOrUpdatePaymentResponseService")
@Slf4j
public class CreateOrUpdatePaymentResponseService implements
    Service<CreateOrUpdatePaymentResponseRequest, CreateOrUpdatePaymentResponseResponse> {

    @Resource @Setter
    private PaymentProcessorManager paymentProcessorManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateOrUpdatePaymentResponseResponse execute(final CreateOrUpdatePaymentResponseRequest request) {
        CreateOrUpdatePaymentResponseResponse response = new CreateOrUpdatePaymentResponseResponse();
        response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
        response.setResponseDescription("error");
        if (!validate(request)) {
            return response;
        }

        PaymentProcessor processor = paymentProcessorManager.retrievePaymentProcessor(request.getPaymentMethodName());
        response = (CreateOrUpdatePaymentResponseResponse) processor.processResponse(request);
        return response;
    }

    private boolean validate(final CreateOrUpdatePaymentResponseRequest request) {
        if (paymentProcessorManager.retrievePaymentProcessor(request.getPaymentMethodName()) == null) {
            log.warn("Payment processor does not exist, unvalid request: [{}]", request);
            return false;
        }
        if (PaymentOperation.fromString(request.getPaymentOperationType()) == null) {
            log.warn("Payment operation not supported, unvalid request: [{}]", request);
            return false;
        }
        return true;
    }
}
