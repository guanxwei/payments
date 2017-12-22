package org.wgx.payments.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.wgx.payments.client.api.Activity;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.client.api.io.CreatePaymentResponse;
import org.wgx.payments.execution.PaymentProcessor;
import org.wgx.payments.execution.PaymentProcessorManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Payments service to create new payments requests.
 *
 */
@Component(value = "createPaymentRequestService")
@Slf4j
public class CreatePaymentRequestActivity implements Activity<CreatePaymentRequest, CreatePaymentResponse> {

    @Resource
    private PaymentProcessorManager paymentProcessorManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public CreatePaymentResponse execute(final CreatePaymentRequest request) {
        if (!validateBasicInfo(request)) {
            return buildErrorResponse();
        }
        if (request.getPaymentMethodName().size() == 1) {
            return invokeV1(request);
        } else {
            log.error("Too much payment methods selected, case not supported!");
            return buildErrorResponse();
        }
    }

    private CreatePaymentResponse invokeV1(final CreatePaymentRequest request) {
        PaymentProcessor paymentProcessor = paymentProcessorManager.retrievePaymentProcessor(request.getPaymentMethodName().get(0));
        if (paymentProcessor == null) {
            log.error("Can not find right payment processor to process the request, something wrong happened.", request);
            return buildErrorResponse();
        }
        log.info("Find payment processor [{}] to process the incoming request", paymentProcessor.getPaymentProcessorName());
        return (CreatePaymentResponse) paymentProcessor.processRequest(request);
    }

    private boolean validateBasicInfo(final CreatePaymentRequest request) {
        if (request.getPaymentMethodName() == null || request.getPaymentMethodName().size() < 1) {
            log.error("Payment method list can not be empty, something wrong happened. Request : [{}]", request);
            return false;
        }
        if (request.getPaymentOperationType() == null || request.getPaymentOperationType().length() == 0) {
            log.error("Payment operation can not be missing, something wrong happened. Request : [{}]", request);
        }
        return true;
    }

    private CreatePaymentResponse buildErrorResponse() {
        CreatePaymentResponse response = new CreatePaymentResponse();
        response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
        response.setResponseDescription("Unvalid request");
        return response;
    }

}
