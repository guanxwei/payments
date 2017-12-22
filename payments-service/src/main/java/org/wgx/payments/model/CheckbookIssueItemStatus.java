package org.wgx.payments.model;

/**
 * Check book item status enumeration.
 *
 */
public enum CheckbookIssueItemStatus {

    // CHECKSTYLE:OFF

    /**
     * Item pending on checking.
     */
    PENDING(1),

    /**
     * Item passed check procedure.
     */
    SUCCESS(2),

    /**
     * Item failed to pass check procedure.
     */
    FAIL(3),

    /**
     * Payment transaction was not processed properly, close it without any
     * side-effect.
     */
    CLOSED(4),

    /**
     * Related payment transaction is being refunded.
     */
    REFUNDING(5),

    /**
     * Related payment transaction has been refunded.
     */
    REFUNDED(6);

    private int status;

    CheckbookIssueItemStatus(final int status) {
        this.status = status;
    }

    public int status() {
        return this.status;
    }

    // CHECKSTYLE:ON
}
