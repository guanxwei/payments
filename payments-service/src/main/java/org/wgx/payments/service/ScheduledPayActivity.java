package org.wgx.payments.service;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.wgx.payments.client.api.Activity;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.ScheduledPayRequest;
import org.wgx.payments.client.api.io.ScheduledPayResponse;
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
 * Payment service to automatically periodic charge money for Premium VIP customers.
 *
 */
@Slf4j
@Component(value = "scheduledPayService")
public class ScheduledPayActivity implements Activity<ScheduledPayRequest, ScheduledPayResponse> {

    @Resource @Setter
    private PaymentProcessorManager paymentProcessorManager;

    @Resource @Setter
    private PaymentRequestDAO paymentRequestDAO;

    @Resource @Setter
    private PaymentResponseDAO paymentResponseDAO;

    @Resource @Setter
    private FastSearchTableDAO fastSearchTableDAO;

    @Resource @Setter
    private Deducer<String, BusinessProfile> businessProfileDeducer;

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledPayResponse execute(final ScheduledPayRequest request) {
        log.info("Incoming scheduled pay request from customer [{}] with business [{}]", request.getCustomerID(),
                businessProfileDeducer.deduce(request.getBusiness()).profile());
        return invokeV2(request);
    }

    private ScheduledPayResponse invokeV2(final ScheduledPayRequest request) {
        log.debug("Version 2 logic is applicated to handle the incoming scheduled pay request!"
                + " Fast search item will be used to check if the transaction is paid or not");

        ScheduledPayResponse response = initiate();
        if (!checkBasicInfo(request)) {
            return response;
        }

        FastSearchTableItem item = loadContract(request);
        if (item == null) {
            log.warn("Customer has not signed contract withe us yet, unvalid scheduled pay request.");
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription(String.format(
                    "Customer has not sign contract with cloud music for business [%s] yet", request.getBusiness()));
            return response;
        }

        response = scheduledPay(request, item);
        return response;
    }

    private boolean checkBasicInfo(final ScheduledPayRequest request) {
        if (StringUtils.isEmpty(request.getCustomerID())) {
            log.warn("Customer id can not be empty.");
            return false;
        }
        if (StringUtils.isEmpty(request.getBusiness())) {
            log.warn("Business can not be empty");
            return false;
        }
        return true;
    }

    private ScheduledPayResponse initiate() {
        ScheduledPayResponse response = new ScheduledPayResponse();
        response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
        response.setResponseDescription("Unvalid request.");
        return response;
    }

    private FastSearchTableItem loadContract(final ScheduledPayRequest request) {
        BusinessProfile profile = businessProfileDeducer.deduce(request.getBusiness());
        FastSearchTableItem item = fastSearchTableDAO.find("sign" + request.getCustomerID() + profile.profile());
        return item;
    }

    private ScheduledPayResponse scheduledPay(final ScheduledPayRequest request, final FastSearchTableItem item) {
        PaymentResponse record = paymentResponseDAO.getPaymentResponseByTransactionID(item.getTransactionID());
        String paymentProcessorName = record.getPaymentMethod();
        SimplePaymentProcessor processor = (SimplePaymentProcessor) paymentProcessorManager.retrievePaymentProcessor(paymentProcessorName);
        log.info("Find payment processor [{}] to handle the incoming scheduled pay request.", processor.getPaymentProcessorName());
        processor.getLocalResponse().set(record);
        request.setPaymentOperationType(PaymentOperation.SCHEDULEDPAY.operationType());
        return (ScheduledPayResponse) processor.processRequest(request);
    }
}
