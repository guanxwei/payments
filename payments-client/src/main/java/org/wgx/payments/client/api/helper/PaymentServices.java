package org.wgx.payments.client.api.helper;

/**
 * Enumeration of payment services.
 *
 */
public enum PaymentServices {

    /**
     * CreateOrUpdatePaymentResponseService.
     */
    CREATE_OR_UPDATE_PAYMENT_RESPONSE_SERVICE("createOrUpdatePaymentResponseService"),

    /**
     * CreatePaymentRequestService.
     */
    CREATE_PAYMENT_REQUEST_SERVICE("createPaymentRequestService"),

    /**
     * QueryPaymentTransactionService.
     */
    QUERY_PAYMENT_TRANSACTION_SERVICE("queryPaymentTransactionService"),

    /**
     * RefundService.
     */
    REFUND_SERVICE("refundService"),

    /**
     * RescindService.
     */
    RESCIND_SERVICE("rescindService"),

    /**
     * ScheduledPayService.
     */
    SCHEDULED_PAY_SERVICE("scheduledPayService");

    private String service;

    /**
     * Constructor.
     * @param service Payment service's name.
     */
    PaymentServices(final String service) {
        this.service = service;
    }

    @Override
    public String toString() {
        return this.service;
    }
}
