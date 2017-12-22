package org.wgx.payments.model;

/**
 * PaymentRequest status enum.
 */
public enum PaymentRequestStatus {

    /**
     * Payment request has been created, pending on payment.
     */
    PENDING(1),

    /**
     * Customer has paid the order.
     */
    PAID(2),

    /**
     * Set the request as unvalid when customer need to repay the order via other payment method.
     */
    UNVALID(3),

    /**
     * Fail to send request to 3P gateway, need to be resent.
     */
    PENDING_ON_RETRY(100),

    /**
     * Payment request has been created, pending on query. Suitable for cases when 3P payment gateway will not initiatively notify
     * Payments-platform.
     */
    PENDING_ON_QUERY(4);

    // CHECKSTYLE:OFF
    private int status;

    private PaymentRequestStatus(final int status) {
        this.status = status;
    }

    public int status() {
        return this.status;
    }
    // CHECKSTYLE:ON
}
