package org.wgx.payments.callback;

/**
 * Callback status.
 *
 */
public enum CallbackStatus {

    /**
     * Callback pending on process.
     */
    PENDING(11),

    /**
     * Callback being processed.
     */
    PROCESSING(12),

    /**
     * Callback has been processed.
     */
    PROCESSED(13);

    private int status;

    /**
     * Default constructor.
     * @param status Callback status.
     */
    CallbackStatus(final int status) {
        this.status = status;
    }

    /**
     * Return callback status.
     * @return Callback status.
     */
    public int status() {
        return this.status;
    }
}
