package org.wgx.payments.builder;

import java.sql.Timestamp;

import org.wgx.payments.model.PaymentRequest;


/**
 * PaymentRequest builder.
 *
 */
public class PaymentRequestBuilder {

    // CHECKSTYLE:OFF
    private String referenceIDList;
    private int status;
    private String channel;
    private Timestamp createTime;
    private Timestamp lastUpdateTime;
    private String requestedAmount;
    private String paymentMethodList;
    private long customerID;
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
        result.setReferenceIDList(referenceIDList);
        result.setStatus(status);
        result.setCreateTime(createTime);
        result.setLastUpdateTime(lastUpdateTime);
        result.setPaymentMethodList(paymentMethodList);
        result.setRequestedAmount(requestedAmount);
        result.setPaymentOperationType(paymentOperationType);
        result.setCallBackMetaInfo(callBackMetaInfo);
        result.setBusiness(business);
        return result;
    }

    // CHECKSTYLE:OFF

    public PaymentRequestBuilder referenceIDList(final String referenceIDList) {
        this.referenceIDList = referenceIDList;
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

    public PaymentRequestBuilder paymentMethodList(final String paymentMethodList) {
        this.paymentMethodList = paymentMethodList;
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
    // CHECKSTYLE:ON
}
