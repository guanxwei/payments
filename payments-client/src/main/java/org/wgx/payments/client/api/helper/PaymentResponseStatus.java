package org.wgx.payments.client.api.helper;

/**
 * Payment response status.
 *
 */
public enum PaymentResponseStatus {

    /**
     * Payment platform has not received response yet.
     */
    PENDING(0),

    /**
     * Payment request has been processed successfully.
     */
    SUCCESS(1),

    /**
     * Once the payment request was processed successfully, but then the customer marked the transaction as unvalid.
     * Mostly used in Sign cases, when the customer rescind with us, we will mark the previous sign request as unvalid.
     */
    UNVALID(2),

    /**
     * Something wrong happened during processing.
     */
    FAIL(3),

    /**
     * Request has been processed properly, but pending on some other procedure.
     */
    PENDING_ON_AUTO_PROCESSING(4),

    /**
     * Auto refund processed.
     */
    AUTO_PROCESSED(5);

    // CHECKSTYLE:OFF
    private int status;

    private PaymentResponseStatus(final int status) {
        this.status = status;
    }

    public int status() {
        return this.status;
    }
    // CHECKSTYLE:ON
}
