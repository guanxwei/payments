package org.wgx.payments.model;

/**
 * Check book item status enumeration.
 *
 */
public enum CheckbookItemStatus {

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
    FAIL(3);

    private int status;

    CheckbookItemStatus(final int status) {
        this.status = status;
    }

    public int status() {
        return this.status;
    }

    //CHECKSTYLE:ON
}
