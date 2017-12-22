package org.wgx.payments.mockbank.alipay;

import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.mockbank.Role;

/**
 * Alipay Rescind role.
 *
 */
public class RescindRole implements Role {

    private static final String NAME = "AilpayRescindRole";

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
        return uri.contains(PaymentMethod.ALIPAY.paymentMethodName()) && uri.contains("gateway")
                && request.getParameter("service").equals("alipay.dut.customer.agreement.unsign");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int level() {
        return 0;
    }

}
