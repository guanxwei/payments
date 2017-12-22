package org.wgx.payments.exception;

/**
 * Exception that will be triggered when communicating with Memcache servers.
 * @author 魏冠雄
 *
 */
public class MemcacheException extends RuntimeException {

    private static final long serialVersionUID = 3959574794642133655L;

    public MemcacheException(final String message) {
        super(message);
    }

    public MemcacheException(final String message, final Throwable t) {
        super(message, t);
    }
}
