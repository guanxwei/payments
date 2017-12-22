package org.wgx.payments.utils;

/**
 * Utility class to store thread isolated values.
 *
 */
public final class ThreadContext {

    private static final ThreadLocal<String> MESSAGE = new ThreadLocal<>();

    private ThreadContext() { }

    /**
     * Get error message.
     * @return Error message
     */
    public static String getMessage() {
        return MESSAGE.get();
    }

    /**
     * Set error message.
     * @param value Error message.
     */
    public static void setMessage(final String value) {
        MESSAGE.set(value);
    }
}
