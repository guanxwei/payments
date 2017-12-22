package org.wgx.payments.flowcontrol;

public class FlowControlException extends RuntimeException {

    private static final long serialVersionUID = -8592814417477977300L;

    public FlowControlException(final String message) {
        super(message);
    }

    public FlowControlException(final Throwable t) {
        super(t);
    }

    public FlowControlException(final String message, final Throwable t) {
        super(message, t);
    }
}
