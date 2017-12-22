package org.wgx.payments.mockbank.wechat;

import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.mockbank.Role;

/**
 * Wechat Rescind role.
 *
 */
public class RescindRole implements Role {

    private static final String NAME = "WechatRescindRole";

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
        return uri.contains(PaymentMethod.WECHAT.paymentMethodName()) && uri.contains("deletecontract");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int level() {
        return 0;
    }

}
