package org.wgx.payments.exception;

/**
 * Exception thrown when the specific {@linkplain Account} is not found.
 *
 */
public class AccountNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 8763765140637137169L;

    /**
     * Constructor with message.
     * @param message Error message.
     */
    public AccountNotFoundException(final String message) {
        super(message);
    }
}
