package org.wgx.payments.exception;

public class PaymentsException extends RuntimeException {

    private static final long serialVersionUID = -4078272901546620341L;

    private int code;

    public PaymentsException(final int code, final String message) {
        super(message);
        this.code = code;
    }

    public PaymentsException(final int code, final String message, final Throwable t) {
        super(message, t);
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
