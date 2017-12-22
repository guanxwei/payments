package org.wgx.payments.builder;

import java.sql.Timestamp;

import org.wgx.payments.model.PaymentRequest;


/**
 * PaymentRequest builder.
 *
 */
public class PaymentRequestBuilder {

    // CHECKSTYLE:OFF
    private String transactionID;
    private String referenceID;
    private int status;
    private String channel;
    private Timestamp createTime;
    private Timestamp lastUpdateTime;
    private String requestedAmount;
    private String paymentMethod;
    private String parentID;
    private String customerID;
    private String url;
    private String paymentOperationType;
    private String callBackMetaInfo;
    private String business;
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
        result.setTransactionID(transactionID);
        result.setReferenceID(referenceID);
        result.setStatus(status);
        result.setCreateTime(createTime);
        result.setLastUpdateTime(lastUpdateTime);
        result.setParentID(parentID);
        result.setPaymentMethod(paymentMethod);
        result.setRequestedAmount(requestedAmount);
        result.setUrl(url);
        result.setPaymentOperationType(paymentOperationType);
        result.setCallBackMetaInfo(callBackMetaInfo);
        result.setBusiness(business);
        return result;
    }

    // CHECKSTYLE:OFF
    public PaymentRequestBuilder transactionID(final String transactionID) {
        this.transactionID = transactionID;
        return this;
    }

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

    public PaymentRequestBuilder paymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public PaymentRequestBuilder parentID(final String parentID) {
        this.parentID = parentID;
        return this;
    }

    public PaymentRequestBuilder customerID(final String customerID) {
        this.customerID = customerID;
        return this;
    }

    public PaymentRequestBuilder url(final String url) {
        this.url = url;
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
    // CHECKSTYLE:ON
}
