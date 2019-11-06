package org.wgx.payments.client.api.helper;

/**
 * Payment operations supported by payments platform.
 *
 */
public enum PaymentOperation {

    /**
     * Sign operation, sign an agreement between the customer and ***.
     */
    SIGN("sign") {
        @Override
        public <T, U> U execute(final Visitor<T, U> visitor, final T request) {
            return visitor.onSign(request);
        }
    },

    /**
     * Sign and charge operation, sign an agreement between the customer and ***,
     * meanwhile charge money from the bank.
     */
    SIGN_AND_CHARGE("sign_and_charge") {
        @Override
        public <T, U> U execute(final Visitor<T, U> visitor, final T request) {
            return visitor.onSignAndCharge(request);
        }
    },

    /**
     * Charge operation, charge money from the bank.
     */
    CHARGE("charge") {
        @Override
        public <T, U> U execute(final Visitor<T, U> visitor, final T request) {
            return visitor.onCharge(request);
        }
    },

    /**
     * Rescind agreement operation.
     */
    RESCIND("rescind") {
        @Override
        public <T, U> U execute(final Visitor<T, U> visitor, final T request) {
            return visitor.onRescind(request);
        }
    },

    /**
     * Scheduled payment operation.
     */
    SCHEDULEDPAY("scheduledpay") {
        @Override
        public <T, U> U execute(final Visitor<T, U> visitor, final T request) {
            return visitor.onScheduledPay(request);
        }
    },

    /**
     * Transfer operation, transfer money to customer.
     */
    TRANSFER("transfer") {
        @Override
        public <T, U> U execute(final Visitor<T, U> visitor, final T request) {
            return visitor.onTransfer(request);
        }
    },

    /**
     * Refund operation, refund money to the customer.
     */
    REFUND("refund") {
        @Override
        public <T, U> U execute(final Visitor<T, U> visitor, final T request) {
            return visitor.onRefund(request);
        }
    };

    // CHECKSTYLE:OFF
    private String operationType;

    private PaymentOperation(String operationType) {
        this.operationType = operationType;
    }

    public String operationType() {
        return this.operationType;
    }

    public static PaymentOperation fromString(final String operationType) {
        for (PaymentOperation operation : PaymentOperation.values()) {
            if (operation.operationType.equals(operationType)) {
                return operation;
            }
        }
        return null;
    }

    /**
     * Execute on the input request via the visitor.
     * @param visitor Visitor who is visiting and processing the input request.
     * @param request Input request.
     * @return Processing result.
     */
    public abstract <T, U> U execute(final Visitor<T, U> visitor, final T request);
    public interface Visitor<T, U> {
        public U onSign(final T request);
        public U onSignAndCharge(final T request);
        public U onCharge(final T request);
        public U onRefund(final T request);
        public U onRescind(final T request);
        public U onScheduledPay(final T request);
        public U onTransfer(final T request);
    }
    // CHECKSTYLE:ON
}
