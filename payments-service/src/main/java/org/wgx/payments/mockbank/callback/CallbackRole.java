package org.wgx.payments.mockbank.callback;

import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.mockbank.Role;

/**
 * Alipay Charge role.
 *
 */
public class CallbackRole implements Role {

    private static final String NAME = "CallbackRole";

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("callback");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int level() {
        return 0;
    }

}
