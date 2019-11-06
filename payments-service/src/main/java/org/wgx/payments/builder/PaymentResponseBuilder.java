package org.wgx.payments.builder;

import java.sql.Timestamp;

import org.wgx.payments.model.PaymentResponse;

/**
 * PaymentResponse builder.
 *
 */
public class PaymentResponseBuilder {

    // CHECKSTYLE:OFF
    private String externalTransactionID;
    private int status;
    private String rawResponse;
    private Timestamp createTime;
    private String acknowledgedAmount;
    private long requestID;
    // CHECKSTYLE:ON

    /**
     * Builder.
     * @return PaymentResponseBuilder instance.
     */
    public static PaymentResponseBuilder builder() {
        return new PaymentResponseBuilder();
    }

    /**
     * Build PaymentResponse object.
     * @return Build PaymentResponse instance.
     */
    public PaymentResponse build() {
        PaymentResponse response = new PaymentResponse();
        response.setCreateTime(createTime);
        response.setExternalTransactionID(externalTransactionID);
        response.setRawResponse(rawResponse);
        response.setStatus(status);
        response.setAcknowledgedAmount(acknowledgedAmount);
        response.setRequestID(requestID);
        return response;
    }

    // CHECKSTYLE:OFF
    public PaymentResponseBuilder externalTransactionID(final String externalTransactionID) {
        this.externalTransactionID = externalTransactionID;
        return this;
    }

    public PaymentResponseBuilder status(final int status) {
        this.status = status;
        return this;
    }

    public PaymentResponseBuilder rawResponse(final String rawResponse) {
        this.rawResponse = rawResponse;
        return this;
    }

    public PaymentResponseBuilder createTime(final Timestamp createTime) {
        this.createTime = createTime;
        return this;
    }

    public PaymentResponseBuilder acknowledgedAmount(final String acknowledgedAmount) {
        this.acknowledgedAmount = acknowledgedAmount;
        return this;
    }

    public PaymentResponseBuilder requestID(final long requestID) {
        this.requestID = requestID;
        return this;
    }
    // CHECKSTYLE:ON
}
