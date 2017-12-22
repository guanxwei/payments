package org.wgx.payments.mockbank.alipay;

import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.mockbank.RoleAction;

/**
 * Alipay verify action mocker.
 *
 */
public class VerifyAction implements RoleAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public String act(final HttpServletRequest request) {
        return "true";
    }

}
