package org.wgx.payments.processors;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.wgx.payments.builder.PaymentRequestBuilder;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentResponseStatus;
import org.wgx.payments.client.api.helper.ResponseStatus;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.client.api.io.CreatePaymentResponse;
import org.wgx.payments.client.api.io.RefundRequest;
import org.wgx.payments.client.api.io.RefundResponse;
import org.wgx.payments.execution.SimplePaymentProcessor;
import org.wgx.payments.model.PaymentRequest;
import org.wgx.payments.model.PaymentRequestStatus;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.ThreadContext;

/**
 * Apple IAP payment processor.
 *
 */
public class IAPProcessor extends SimplePaymentProcessor {

    private static final String NAME = PaymentMethod.IAP.paymentMethodName();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPaymentProcessorName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CreatePaymentResponse invokeChargeOperation(final CreatePaymentRequest request) {
        /**
         * For IAP payment processor, we don't need to provide anything to up-stream service but to.
         * add a record in DB.
         *
         */
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        String referenceID = getReference(request.getReferences());
        PaymentRequest paymentRequest = PaymentRequestBuilder.builder()
                .channel("Iphone")
                .createTime(now)
                .lastUpdateTime(now)
                .paymentMethod(getPaymentProcessorName())
                .paymentOperationType(request.getPaymentOperationType())
                .url(StringUtils.EMPTY)
                .requestedAmount(request.getReferences().get(referenceID))
                .status(PaymentRequestStatus.PENDING.status())
                .referenceID(referenceID)
                .transactionID(getTransactionID().get())
                .build();
        boolean succeed = getPaymentRequestDAO().save(paymentRequest) == 1;
        CreatePaymentResponse response = new CreatePaymentResponse();
        if (succeed) {
            response.setResponseCode(ResponseStatus.PROCESS_SUCCESS_CODE);
            response.setTransactionID(paymentRequest.getTransactionID());
        } else {
            response.setResponseCode(ResponseStatus.INTERNAL_ERROR_CODE);
            response.setResponseDescription("DB is not reachable, please try again later.");
        }
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefundResponse invokeRefundOperation(final RefundRequest request) {
        return (RefundResponse) logAndReturn(request, REQUEST_TO_RESPONSE_MAPPING.get(request.getClass()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTransactionID(final CreateOrUpdatePaymentResponseRequest request) {
        return request.getParameters().get("transactionID")[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExternalTransactionID(final CreateOrUpdatePaymentResponseRequest request) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatus(final CreateOrUpdatePaymentResponseRequest request) {
        return PaymentResponseStatus.SUCCESS.status();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String success() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("status", "SUCCESS");
        resultMap.put("code", ThreadContext.getMessage());
        return Jackson.json(resultMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String fail() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("status", "FAIL");
        resultMap.put("code", ThreadContext.getMessage());
        return Jackson.json(resultMap);
    }
}
