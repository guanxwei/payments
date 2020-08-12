package org.wgx.payments.providers;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.stream.core.execution.Engine;
import org.stream.core.execution.GraphContext;
import org.stream.core.execution.WorkFlowContext;
import org.stream.core.resource.ResourceTank;
import org.wgx.payments.client.api.Service;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.client.api.io.CreatePaymentResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Payments service to create new payments requests.
 *
 */
@Component(value = "createPaymentRequestService")
@Slf4j
public class CreatePaymentRequestService implements Service<CreatePaymentRequest, CreatePaymentResponse> {

    private static final String CREATE_PAYMENTS_REQUEST_REFERENCE = "Create::Payments::Request::Reference";

    @Resource
    private Engine engine;

    @Resource
    private GraphContext graphContext;

    @Value(value = "${create.payments.graph}")
    private String graphName;

    /**
     * {@inheritDoc}
     */
    @Override
    public CreatePaymentResponse execute(final CreatePaymentRequest request) {
        if (!validateBasicInfo(request)) {
            return buildErrorResponse();
        }

        org.stream.core.resource.Resource primary = org.stream.core.resource.Resource.builder()
                .resourceReference(CREATE_PAYMENTS_REQUEST_REFERENCE)
                .value(request)
                .build();

        ResourceTank resourceTank = engine.executeOnce(graphContext, graphName, primary, false);

        org.stream.core.resource.Resource response = resourceTank.resolve(WorkFlowContext.WORK_FLOW_RESPONSE_REFERENCE);
        CreatePaymentResponse createPaymentResponse = response.resolveValue(CreatePaymentResponse.class);
        log.info("Create payments response generated [{}]", createPaymentResponse);
        return createPaymentResponse;
    }

    private boolean validateBasicInfo(final CreatePaymentRequest request) {
        if (CollectionUtils.isEmpty(request.getPaymentMethod())) {
            log.error("Payment method list can not be empty, something wrong happened. Request : [{}]", request);
            return false;
        }
        if (request.getPaymentOperationType() == null || request.getPaymentOperationType().length() == 0) {
            log.error("Payment operation can not be missing, something wrong happened. Request : [{}]", request);
        }
        long thirdPartyMethods = request.getPaymentMethod().stream()
                .filter(method -> PaymentMethod.fromCode(method) != null)
                .filter(method -> !PaymentMethod.fromCode(method).isInternal())
                .count();
        if (thirdPartyMethods > 1) {
            log.error("Only one third party payment method can be selected");
            return false;
        }

        long internalPartyMethods = request.getPaymentMethod().stream()
                .filter(method -> PaymentMethod.fromCode(method) != null)
                .filter(method -> PaymentMethod.fromCode(method).isInternal())
                .count();
        if (internalPartyMethods > 1) {
            log.error("Only one internal payment method can be selected");
            return false;
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
