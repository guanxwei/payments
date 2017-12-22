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
    private String transactionID;
    private int status;
    private String rawResponse;
    private Timestamp createTime;
    private Timestamp lastUpdateTime;
    private String referenceID;
    private String paymentMethod;
    private String operationType;
    private String business;
    private String customerID;
    private String acknowledgedAmount;
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
        response.setTransactionID(transactionID);
        response.setLastUpdateTime(lastUpdateTime);
        response.setReferenceID(referenceID);
        response.setPaymentMethod(paymentMethod);
        response.setOperationType(operationType);
        response.setBusiness(business);
        response.setCustomerID(customerID);
        response.setAcknowledgedAmount(acknowledgedAmount);
        return response;
    }

    // CHECKSTYLE:OFF
    public PaymentResponseBuilder externalTransactionID(String externalTransactionID) {
        this.externalTransactionID = externalTransactionID;
        return this;
    }

    public PaymentResponseBuilder transactionID(String transactionID) {
        this.transactionID = transactionID;
        return this;
    }

    public PaymentResponseBuilder status(int status) {
        this.status = status;
        return this;
    }

    public PaymentResponseBuilder rawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
        return this;
    }

    public PaymentResponseBuilder createTime(Timestamp createTime) {
        this.createTime = createTime;
        return this;
    }

    public PaymentResponseBuilder referenceID(String referenceID) {
        this.referenceID = referenceID;
        return this;
    }

    public PaymentResponseBuilder paymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public PaymentResponseBuilder lastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    public PaymentResponseBuilder operationType(String operationType) {
        this.operationType = operationType;
        return this;
    }

    public PaymentResponseBuilder business(String business) {
        this.business = business;
        return this;
    }

    public PaymentResponseBuilder customerID(String customerID) {
        this.customerID = customerID;
        return this;
    }

    public PaymentResponseBuilder acknowledgedAmount(String acknowledgedAmount) {
        this.acknowledgedAmount = acknowledgedAmount;
        return this;
    }
    // CHECKSTYLE:ON
}
