package org.wgx.payments.providers;

import java.sql.Timestamp;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.wgx.payments.builder.FastSearchTableItemBuilder;
import org.wgx.payments.client.api.Activity;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.client.api.helper.PaymentOperation;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.RefundRequest;
import org.wgx.payments.client.api.io.RefundResponse;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.deducer.Deducer;
import org.wgx.payments.execution.PaymentProcessorManager;
import org.wgx.payments.execution.SimplePaymentProcessor;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.model.PaymentResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment service to refund.
 *
 */
@Slf4j
@Component(value = "refundService")
public class RefundActivity implements Activity<RefundRequest, RefundResponse> {

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
    public RefundResponse execute(final RefundRequest request) {
        log.info("Incoming refund request for reference [{}] whose charge transaction id is [{}]", request.getReferenceID(), request.getTransactionID());
        return invokeV2(request);
    }

    /**
     * Using fast search item to check if the transaction has be paid or not.
     * @param request Refund request.
     * @return Refund response.
     */
    private RefundResponse invokeV2(final RefundRequest request) {
        log.info("Version 2 refund logic is applicated to handle the incoming refund request!"
                + " Fast search item will be used to check if the transaction is paid or not");
        RefundResponse response = new RefundResponse();
        response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);

        // Duplicated refund request check.
        if (isDuplicatedRequest(request)) {
            response.setResponseCode(400);
            response.setResponseDescription("Duplicated Request!");
            return response;
        }

        // Fetch charge response, preparing refund context.
        PaymentResponse chargeResponse = preparePaymentResponse(request);

        // Execute the refund action.
        if (chargeResponse == null) {
            response.setResponseDescription("Can not find corresponding charge response! Request denied!");
        } else if (chargeResponse.getStatus() != PaymentResponseStatus.SUCCESS.status()
                && chargeResponse.getStatus() != PaymentResponseStatus.PENDING_ON_AUTO_PROCESSING.status()) {
            response.setResponseDescription("The refund request's corresponding charge request has not been paid successfully!");
        } else {
            SimplePaymentProcessor processor = (SimplePaymentProcessor) paymentProcessorManager.retrievePaymentProcessor(chargeResponse.getPaymentMethod());
            log.info("Find processor [{}] to handle the incoming refund request!", processor.getPaymentProcessorName());
            processor.getLocalResponse().set(chargeResponse);
            request.setPaymentOperationType(PaymentOperation.REFUND.operationType());
            response = (RefundResponse) processor.processRequest(request);
        }
        return response;
    }

    private boolean isDuplicatedRequest(final RefundRequest request) {
        if (request.getExternalRefundID() != null) {
            FastSearchTableItem duplicateItem = fastSearchTableDAO.find(request.getBusiness() + request.getExternalRefundID());
            if (duplicateItem != null) {
                log.info("Duplicate request, reject!");
                return true;
            }
            duplicateItem = FastSearchTableItemBuilder.builder()
                    .itemKey(request.getBusiness() + request.getExternalRefundID())
                    .message("Key")
                    .status(0)
                    .time(new Timestamp(System.currentTimeMillis()))
                    .transactionID(request.getTransactionID())
                    .build();
            fastSearchTableDAO.save(duplicateItem);
        }
        return false;
    }

    private PaymentResponse preparePaymentResponse(final RefundRequest request) {
        PaymentResponse chargeResponse = paymentResponseDAO.getPaymentResponseByTransactionID(request.getTransactionID());
        BusinessProfile profile = businessProfileDeducer.deduce(request.getBusiness());
        if (chargeResponse == null) {
            log.info(String.format("Can not find existed record by transaction [%s], whose referenceID is [%s], business is [%s]",
                    request.getTransactionID(), request.getReferenceID(), request.getBusiness()));
            String key = "charge" + request.getReferenceID() + profile.profile();
            FastSearchTableItem item = fastSearchTableDAO.find(key);
            if (item != null) {
                chargeResponse = paymentResponseDAO.getPaymentResponseByTransactionID(item.getTransactionID());
            }
        }
        return chargeResponse;
    }
}
