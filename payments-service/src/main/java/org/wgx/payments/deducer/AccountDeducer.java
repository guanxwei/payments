package org.wgx.payments.deducer;

import org.apache.commons.lang3.tuple.Pair;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.io.Request;
import org.wgx.payments.exception.AccountNotFoundException;
import org.wgx.payments.virtual.account.PaymentAccountClient;
import org.wgx.payments.virtual.account.io.PaymentAccountRequest;
import org.wgx.payments.virtual.account.io.PaymentAccountResponse;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Deducer to deduce pay-account according to the incoming request.
 *
 */
@Data
@Slf4j
public class AccountDeducer implements Deducer<Pair<Request, Integer>, Pair<String, String>> {

    private PaymentAccountClient paymentAccountClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<String, String> deduce(final Pair<Request, Integer> request) {
        Pair<String, String> pair = Pair.of(null, null);
        PaymentAccountRequest paymentAccountRequest = new PaymentAccountRequest();
        paymentAccountRequest.setBusiness(request.getLeft().getBusiness());
        if (request.getRight().equals(PaymentMethod.WECHAT.paymentMethodCode())) {
            paymentAccountRequest.setDeviceType(request.getLeft().getChannel());
        }
        paymentAccountRequest.setPaymentOperation(request.getLeft().getPaymentOperationType());
        paymentAccountRequest.setPaymentMethod(request.getRight());
        PaymentAccountResponse paymentAccountResponse = paymentAccountClient.getPaymentAccount(paymentAccountRequest);
        log.info("Receive response [{}] from payment account service");
        if (paymentAccountResponse.getAccountName() == null) {
            throw new AccountNotFoundException("Can not deduce account name");
        }
        pair = Pair.of(paymentAccountResponse.getAccountName(), paymentAccountResponse.getAccountNo());
        return pair;
    }

}
