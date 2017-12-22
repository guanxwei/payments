package org.wgx.payments.client.api.helper;

/**
 * Response status constant holder.
 */
public final class ResponseStatus {

    private ResponseStatus() { }

    /**
     * Success response code means the request has been processed successfully by PaymentsPlatform,
     * but we are still pending on the 3P payment gateway's process.
     */
    public static final int PROCESS_SUCCESS_CODE = 200;

    /**
     * Error code.
     */
    public static final int INTERNAL_ERROR_CODE = 500;

    /**
     * Response code indicates that the request not only has been processed by PaymentsPlatform but also processed by 3P payment gateway successfully.
     * When Clients receive this status, then they don't need to wait for the PaymentsPlatform's notification, they can just update the item's status
     * Immediately.
     *
     */
    public static final int PROCESS_COMPLETE_CODE = 10000;
}
