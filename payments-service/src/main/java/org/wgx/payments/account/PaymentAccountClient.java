package org.wgx.payments.account;

import javax.annotation.Resource;

import org.wgx.payments.account.io.PaymentAccountRequest;
import org.wgx.payments.account.io.PaymentAccountResponse;


/**
 * PaymentAccount service's client provided for upstream clients, upstream clients can use this entity to communicate with
 * PaymentAccount service.
 *
 */
public class PaymentAccountClient {

    @Resource(name = "paymentAccountService")
    private PaymentAccountService paymentAccountService;

    /**
     * Call payment account service to get payment account information.
     * @param request Payment account request.
     * @return Payment account response.
     */
    public PaymentAccountResponse getPaymentAccount(final PaymentAccountRequest request) {
        return paymentAccountService.execute(request);
    }
}
