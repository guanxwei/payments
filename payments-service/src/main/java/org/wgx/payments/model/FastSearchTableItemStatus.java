package org.wgx.payments.model;

/**
 * Fast search item status enums.
 *
 */
public enum FastSearchTableItemStatus {

    /**
     * Item pending on process.
     */
    PENDING(1),

    /**
     * Item being processing.
     */
    PROCESSING(2),

    /**
     * Item been processed.
     */
    PROCESSED(3),

    /**
     * Item being retried.
     */
    RETRYING(4);

    private int status;

    /**
     * Default constructor.
     * @param status Item's status.
     */
    FastSearchTableItemStatus(final int status) {
        this.status = status;
    }

    /**
     * Return item's status.
     * @return Item status.
     */
    public int status() {
        return this.status;
    }
}
