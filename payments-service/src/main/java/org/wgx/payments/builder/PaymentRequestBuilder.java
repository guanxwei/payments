package org.wgx.payments.builder;

import java.sql.Timestamp;

import org.wgx.payments.model.PaymentRequest;


/**
 * PaymentRequest builder.
 *
 */
public class PaymentRequestBuilder {

    // CHECKSTYLE:OFF
    private String referenceID;
    private int status;
    private String channel;
    private Timestamp createTime;
    private Timestamp lastUpdateTime;
    private String requestedAmount;
    private int paymentMethod;
    private long customerID;
    private String paymentOperationType;
    private String callBackMetaInfo;
    private String business;
    private long parentRequestID;
    private String transactionID;
    // CHECKSTYLE:ON

    /**
     * Builder.
     * @return PaymentRequestBuilder instance.
     */
    public static PaymentRequestBuilder builder() {
        return new PaymentRequestBuilder();
    }

    /**
     * Build PaymentRequest object.
     * @return Built PaymentRequest instance.
     */
    public PaymentRequest build() {
        PaymentRequest result = new PaymentRequest();
        result.setChannel(channel);
        result.setCustomerID(customerID);
        result.setReferenceID(referenceID);
        result.setStatus(status);
        result.setCreateTime(createTime);
        result.setLastUpdateTime(lastUpdateTime);
        result.setPaymentMethod(paymentMethod);
        result.setRequestedAmount(requestedAmount);
        result.setPaymentOperationType(paymentOperationType);
        result.setCallBackMetaInfo(callBackMetaInfo);
        result.setBusiness(business);
        result.setTransactionID(transactionID);
        result.setParentRequestID(parentRequestID);
        return result;
    }

    // CHECKSTYLE:OFF

    public PaymentRequestBuilder referenceID(final String referenceID) {
        this.referenceID = referenceID;
        return this;
    }

    public PaymentRequestBuilder status(final int status) {
        this.status = status;
        return this;
    }

    public PaymentRequestBuilder channel(final String channel) {
        this.channel = channel;
        return this;
    }

    public PaymentRequestBuilder createTime(final Timestamp createTime) {
        this.createTime = createTime;
        return this;
    }

    public PaymentRequestBuilder lastUpdateTime(final Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    public PaymentRequestBuilder requestedAmount(final String requestedAmount) {
        this.requestedAmount = requestedAmount;
        return this;
    }

    public PaymentRequestBuilder paymentMethod(final int paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public PaymentRequestBuilder customerID(final long customerID) {
        this.customerID = customerID;
        return this;
    }

    public PaymentRequestBuilder paymentOperationType(final String paymentOperationType) {
        this.paymentOperationType = paymentOperationType;
        return this;
    }

    public PaymentRequestBuilder callBackMetaInfo(final String callBackMetaInfo) {
        this.callBackMetaInfo = callBackMetaInfo;
        return this;
    }

    public PaymentRequestBuilder business(final String business) {
        this.business = business;
        return this;
    }

    public PaymentRequestBuilder transactionID(final String transactionID) {
        this.transactionID = transactionID;
        return this;
    }

    public PaymentRequestBuilder parentRequestID(final long parentRequestID) {
        this.parentRequestID = parentRequestID;
        return this;
    }
    // CHECKSTYLE:ON
}
