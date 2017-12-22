package org.wgx.payments.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.account.impl.meta.PaymentAccountScope;
import org.wgx.payments.dao.PaymentAccountDAO;
import org.wgx.payments.dao.PaymentAccountScopeDAO;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.tools.JsonObject;

import lombok.extern.slf4j.Slf4j;

/**
 * Payment account scope manage controller.
 *
 */
@RestController
@RequestMapping(path = "/api/store/backend/payments/accountscope")
@Slf4j
public class AccountScopeController {

    @Resource
    private PaymentAccountDAO paymentAccountDAO;

    @Resource
    private PaymentAccountScopeDAO paymentAccountScopeDAO;

    /**
     * Add new scope for the specific account.
     * @param json Jsonfied {@linkplain PaymentAccountScope} entity.
     * @return Manipulation result.
     */
    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public JsonObject addScope(@RequestParam(name = "json", required = true) final String json) {
        PaymentAccountScope scope = parse(json);

        if (scope == null) {
            return JsonObject.start().code(400).msg("Unvalid request data");
        }

        if (paymentAccountScopeDAO.getAccountCount(scope.getAccountID()) >= 1) {
            return JsonObject.start().code(400).msg("Duplicated request");
        }

        paymentAccountScopeDAO.save(scope);
        return JsonObject.start().code(200);
    }

    /**
     * Edit scope for the specific account.
     * @param json Jsonfied {@linkplain PaymentAccountScope} entity.
     * @return Manipulation result.
     */
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public JsonObject editScope(@RequestParam(name = "json", required = true) final String json) {
        PaymentAccountScope scope = parse(json);

        if (scope == null) {
            return JsonObject.start().code(400).msg("Unvalid request data");
        }

        PaymentAccountScope raw = paymentAccountScopeDAO.find(scope.getAccountID());
        if (raw == null || raw.getId() != scope.getId()) {
            return JsonObject.start().code(404).msg("Chaotic data");
        }

        paymentAccountScopeDAO.edit(scope);
        return JsonObject.start().code(200);
    }

    /**
     * Query payment account scope.
     * @param accountID Payment account id.
     * @return Manipulation result.
     */
    @RequestMapping(path = "/query", method = RequestMethod.GET)
    public JsonObject query(@RequestParam(required = true, name = "accountID") final long accountID) {
        return JsonObject.start().code(200).data(paymentAccountScopeDAO.find(accountID));
    }

    private PaymentAccountScope parse(final String json) {
        PaymentAccountScope scope = null;
        try {
            scope = Jackson.parse(json, PaymentAccountScope.class);
            if (scope.getAccountID() <= 0 || scope.getSupportedBusinesses() == null
                    || scope.getSupportedOperations() == null) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            log.info("Unvalid request", e);
            return null;
        }
        return scope;
    }
}
