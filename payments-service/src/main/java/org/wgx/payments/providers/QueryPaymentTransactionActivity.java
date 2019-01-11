package org.wgx.payments.providers;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.wgx.payments.client.api.Activity;
import org.wgx.payments.client.api.helper.BusinessProfile;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.QueryPaymentTransactionRequest;
import org.wgx.payments.client.api.io.QueryPaymentTransactionResponse;
import org.wgx.payments.dao.FastSearchTableDAO;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.deducer.Deducer;
import org.wgx.payments.model.FastSearchTableItem;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment service to query payment transaction status.
 *
 */
@Component(value = "queryPaymentTransactionService")
@Slf4j
public class QueryPaymentTransactionActivity implements
    Activity<QueryPaymentTransactionRequest, QueryPaymentTransactionResponse> {

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
    public QueryPaymentTransactionResponse execute(final QueryPaymentTransactionRequest request) {
        QueryPaymentTransactionResponse response = new QueryPaymentTransactionResponse();
        if (!validate(request)) {
            log.error("Unvalid request : [{}]", request);
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription("Unvalid Request.");
        } else {
            response = invokeV1(request);
        }
        return response;
    }

    private boolean validate(final QueryPaymentTransactionRequest request) {
        if (StringUtils.isNotBlank(request.getTransactionID())) {
            return true;
        }
        if (StringUtils.isNotBlank(request.getBusiness()) && StringUtils.isNotBlank(request.getReferenceID())) {
            return true;
        }
        return false;
    }

    private QueryPaymentTransactionResponse invokeV1(final QueryPaymentTransactionRequest request) {
        log.info(String.format("Received query request for transaction [%s], referenceID [%S], business [%s]", request.getTransactionID(),
                request.getReferenceID(), request.getBusiness()));
        QueryPaymentTransactionResponse response = new QueryPaymentTransactionResponse();
        response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
        PaymentRequest paymentRequest;
        PaymentResponse paymentResponse;
        if (StringUtils.isNotBlank(request.getTransactionID())) {
            // Query by transaction ID.
            paymentRequest = paymentRequestDAO.getPaymentRequestByTransactionID(request.getTransactionID());
            paymentResponse = paymentResponseDAO.getPaymentResponseByTransactionID(request.getTransactionID());
            if (paymentResponse != null) {
                response.setResponseCode(ResponseStatus.PROCESS_COMPLETE_CODE);
                response.setTransactionStatus(paymentResponse.getStatus());
                response.setTransactionID(paymentResponse.getTransactionID());
                return response;
            } else if (paymentRequest != null) {
                response.setResponseCode(ResponseStatus.PROCESS_COMPLETE_CODE);
                response.setTransactionStatus(PaymentResponseStatus.PENDING.status());
            } else {
                // Currently we do nothing.
            }
        }
        if (StringUtils.isNotBlank(request.getBusiness()) && StringUtils.isNotBlank(request.getReferenceID())) {
            BusinessProfile profile = businessProfileDeducer.deduce(request.getBusiness());
            String key = request.getOperationType() + request.getReferenceID() + profile.profile();
            log.info(String.format("Try to find fast search item via key [%s]", key));
            FastSearchTableItem item = fastSearchTableDAO.find(key);
            if (item != null) {
                response.setResponseCode(ResponseStatus.PROCESS_COMPLETE_CODE);
                response.setTransactionStatus(PaymentResponseStatus.SUCCESS.status());
                response.setTransactionID(item.getTransactionID());
            } else {
                response.setResponseCode(ResponseStatus.PROCESS_COMPLETE_CODE);
                response.setTransactionStatus(PaymentResponseStatus.PENDING.status());
            }
        } else {
            response.setResponseDescription("Unvalid Request.");
        }
        return response;
    }
}
