package org.wgx.payments.account;

import org.wgx.payments.account.io.PaymentAccountRequest;
import org.wgx.payments.account.io.PaymentAccountResponse;

/**
 * Payment account service RPC API interface.
 *
 */
public interface PaymentAccountService {

    /**
     * Call payment account service to get proper account according to
     * the provided information.
     * @param request Payment account request.
     * @return Payment account response.
     */
    PaymentAccountResponse execute(final PaymentAccountRequest request);
}
