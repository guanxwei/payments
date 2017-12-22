package org.wgx.payments.mockbank.alipay;

import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.mockbank.Role;

/**
 * Alipay Charge role.
 *
 */
public class ChargeRole implements Role {

    private static final String NAME = "AilpayChargeRole";

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
                && (request.getParameter("service").equals("create_direct_pay_by_user")
                        || request.getParameter("service").equals("mobile.securitypay.pay"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int level() {
        return 0;
    }

}
