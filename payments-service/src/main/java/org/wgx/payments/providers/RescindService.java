package org.wgx.payments.providers;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.wgx.payments.client.api.Service;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.RescindRequest;
import org.wgx.payments.client.api.io.RescindResponse;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.deducer.Deducer;
import org.wgx.payments.execution.PaymentProcessorManager;
import org.wgx.payments.execution.SimplePaymentProcessor;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.model.PaymentResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment service to rescind agreement with customer.
 *
 */
@Component(value = "rescindService")
@Slf4j
public class RescindService implements Service<RescindRequest, RescindResponse> {

    @Resource @Setter
    private PaymentRequestDAO paymentRequestDAO;

    @Resource @Setter
    private PaymentResponseDAO paymentResponseDAO;

    @Resource @Setter
    private PaymentProcessorManager paymentProcessorManager;

    @Resource @Setter
    private FastSearchTableDAO fastSearchTableDAO;

    @Resource @Setter
    private Deducer<String, BusinessProfile> businessProfileDeducer;

    /**
     * {@inheritDoc}
     */
    @Override
    public RescindResponse execute(final RescindRequest request) {
        log.info("Incoming rescind request for customer [{}], business [{}]", request.getPaymentMethodName(),
                businessProfileDeducer.deduce(request.getBusiness()).profile());

        request.setPaymentOperationType(PaymentOperation.RESCIND.operationType());
        return invokeV2(request);
    }

    /**
     * Using fast search item to check if the customer has signed contract with us or not.
     * @param request Rescind request.
     * @return Rescind response.
     */
    private RescindResponse invokeV2(final RescindRequest request) {
        log.debug("Version 2 logic is applicated to handle the incoming rescind request."
                + " Fast search item will be used to check if the transaction is paid or not");

        RescindResponse rescindResponse = new RescindResponse();

        FastSearchTableItem signedContract = loadContract(request, rescindResponse);
        if (signedContract == null) {
            return rescindResponse;
        }

        PaymentResponse signedResponse = paymentResponseDAO.getPaymentResponseByTransactionID(signedContract.getTransactionID());
        if (signedResponse.getStatus() != PaymentResponseStatus.SUCCESS.status()) {
            rescindResponse.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            rescindResponse.setResponseDescription("Customer has not signed contract with Cloud music yet.");
            return rescindResponse;
        }

        SimplePaymentProcessor processor = (SimplePaymentProcessor) paymentProcessorManager.retrievePaymentProcessor(
                signedResponse.getPaymentMethod());
        log.info("Find payment processor [{}] to handle the request", processor.getPaymentProcessorName());
        processor.getLocalResponse().set(signedResponse);
        rescindResponse = (RescindResponse) processor.processRequest(request);
        return rescindResponse;
    }

    private FastSearchTableItem loadContract(final RescindRequest request, final RescindResponse rescindResponse) {
        BusinessProfile profile = businessProfileDeducer.deduce(request.getBusiness());
        String key = "sign" + request.getCustomerID() + profile.profile();
        FastSearchTableItem item = fastSearchTableDAO.find(key);
        rescindResponse.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
        rescindResponse.setResponseDescription("Customer has not signed contract with Cloud music yet.");
        return item;
    }
}
