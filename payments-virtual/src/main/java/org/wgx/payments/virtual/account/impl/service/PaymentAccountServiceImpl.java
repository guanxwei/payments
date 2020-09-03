package org.wgx.payments.virtual.account.impl.service;

import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.wgx.payments.virtual.account.PaymentAccountService;
import org.wgx.payments.virtual.account.dao.PaymentAccountDAO;
import org.wgx.payments.virtual.account.dao.PaymentAccountScopeDAO;
import org.wgx.payments.virtual.account.impl.meta.PaymentAccount;
import org.wgx.payments.virtual.account.impl.meta.PaymentAccountScope;
import org.wgx.payments.virtual.account.io.PaymentAccountRequest;
import org.wgx.payments.virtual.account.io.PaymentAccountResponse;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@linkplain PaymentAccountService} implementation.
 *
 */
@Service(value = "paymentAccountService")
@Slf4j
public class PaymentAccountServiceImpl implements PaymentAccountService {

    @Setter
    @Resource(name = "paymentAccountDAO")
    private PaymentAccountDAO paymentAccountDAO;

    @Setter
    @Resource(name = "paymentAccountScopeDAO")
    private PaymentAccountScopeDAO paymentAccountScopeDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentAccountResponse execute(final PaymentAccountRequest request) {
        log.info("Receive request to retrive payment account");
        PaymentAccountResponse response = new PaymentAccountResponse();
        List<PaymentAccount> accounts = paymentAccountDAO.getByPaymentMethod(request.getPaymentMethod());
        if (accounts == null || accounts.isEmpty()) {
            response.setCode(404);
            response.setMessage("Can not find payment account associated to payment method " + request.getPaymentMethod());
            return response;
        }

        Optional<PaymentAccount> paymentAccountOptional = accounts.parallelStream()
                .filter(paymentAccount -> {
                    return filter(paymentAccount, request);
                })
                .findAny();

        if (!paymentAccountOptional.isPresent()) {
            response.setCode(404);
            response.setMessage("Can not find payment account scope associated to payment method " + request.getPaymentMethod());
            return response;
        }

        PaymentAccount account = paymentAccountOptional.get();
        response.setAccountName(account.getAccountName());
        response.setAccountNo(account.getAccountNo());
        response.setCode(200);
        return response;
    }

    /**
     * Currently, we only check if the request's business and deviceType.
     * @param paymentAccount Payment account.
     * @param request Payment account request.
     * @return Predicate result.
     */
    private boolean filter(final PaymentAccount paymentAccount, final PaymentAccountRequest request) {
        PaymentAccountScope scope = paymentAccountScopeDAO.find(paymentAccount.getId());
        // If the payment account has not been configured yet, we will not treat it as candidate.
        if (scope == null || request.getBusiness() == null || request.getPaymentOperation() == null) {
            return false;
        }

        if (request.getDeviceType() != null) {
            if (scope.getDeviceType() == null) {
                return false;
            } else if (!scope.getDeviceType().contains(request.getDeviceType())) {
                return false;
            }
        }

        if (!scope.getSupportedBusinesses().contains(request.getBusiness())) {
            return false;
        }

        if (!scope.getSupportedOperations().contains(request.getPaymentOperation())) {
            return false;
        }

        if (request.getIssueingBank() != null && scope.getIssuingBanks() != null
                && scope.getIssuingBanks().contains(request.getIssueingBank())) {
            return false;
        }

        return true;
    }
}
