package org.wgx.payments.model;

import lombok.Data;

/**
 * 
 * @author weiguanxiong
 *
 */
@Data
public class PaymentExecutionRecord {

    // Auto increased id.
    private long id;

    // A external key reference to the payment request entity.
    private long paymentRequestID;

    // The time this execution request was post.
    private long executeTime;

    // Execution status.
    private int status;

    // Amount to be used for this execution.
    private String requestAmount;

    // External transaction id.
    private String externalTransactionID;

    // Acknowledged amount.
    private String acknowledgedAmount;

    // The time this execution request was completed.
    private long completeTime;

    // A reference to the upstream trade system, typically an order id will be stored.
    private String referenceID;
}
