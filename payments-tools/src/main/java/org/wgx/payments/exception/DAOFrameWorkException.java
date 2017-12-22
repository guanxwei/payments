package org.wgx.payments.exception;

public class DAOFrameWorkException extends RuntimeException {

    private static final long serialVersionUID = -908066942258939880L;

    public DAOFrameWorkException(final String message) {
        super(message);
    }

    public DAOFrameWorkException(final String message, final Throwable t) {
        super(message, t);
    }
}
