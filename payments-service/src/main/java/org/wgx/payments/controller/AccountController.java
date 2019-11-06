package org.wgx.payments.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.wgx.payments.account.impl.meta.PaymentAccount;
import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.dao.PaymentAccountDAO;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.tools.JsonObject;

import lombok.extern.slf4j.Slf4j;

/**
 * Payment account manage controller.
 * @author hzweiguanxiong
 *
 */
@RestController
@RequestMapping(path = "/api/store/backend/payments/account")
@Slf4j
public class AccountController {

    @Resource(name = "paymentAccountDAO")
    private PaymentAccountDAO paymentAccountDAO;

    /**
     * Create new payment account.
     * @param json Jsonfied {@linkplain PaymentAccount} entity.
     * @return Manipulation result.
     */
    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public JsonObject createAccount(@RequestParam(required = true, name = "json") final String json) {

        PaymentAccount paymentAccount = parse(json);
        if (paymentAccount == null) {
            return JsonObject.start().code(400).msg("数据不符合要求");
        }

        if (paymentAccountDAO.getAccountNameCount(paymentAccount.getAccountName()) >= 1) {
            return JsonObject.start().code(400).msg("账户名称冲突，账户已存在");
        }

        paymentAccountDAO.save(paymentAccount);
        return JsonObject.start().code(200).data(paymentAccount.getId());
    }

    /**
     * Edit payment account.
     * @param json Jsonfied {@linkplain PaymentAccount} entity.
     * @return Manipulation result.
     */
    @RequestMapping(path = "/edit", method = RequestMethod.POST)
    public JsonObject editAccount(@RequestParam(required = true, name = "json") final String json) {

        PaymentAccount paymentAccount = parse(json);
        if (paymentAccount == null) {
            return JsonObject.start().code(400).msg("数据不符合要求");
        }

        PaymentAccount raw = paymentAccountDAO.get(paymentAccount.getId());
        if (!raw.getAccountName().equals(paymentAccount.getAccountName())
                || raw.getPaymentMethod() != paymentAccount.getPaymentMethod()) {
            return JsonObject.start().code(400).msg("当前仅支持修改account no.");
        }

        paymentAccountDAO.edit(paymentAccount);
        return JsonObject.start().code(200);
    }

    /**
     * Query payment account.
     * @param key Query key word.
     * @param id Payment account id.
     * @return Manipulation result.
     */
    @RequestMapping(path = "/query", method = RequestMethod.GET)
    public JsonObject query(
            @RequestParam(name = "key", required = false, defaultValue = "") final String key,
            @RequestParam(name = "id", required = false, defaultValue = "0") final long id) {
        if (id > 0) {
            return JsonObject.start().code(200).data(paymentAccountDAO.get(id));
        }
        return JsonObject.start().code(200).data(paymentAccountDAO.query(key));
    }

    private PaymentAccount parse(final String json) {
        PaymentAccount paymentAccount = null;
        try {
            paymentAccount = Jackson.parse(json, PaymentAccount.class);
            if (paymentAccount.getAccountName() == null || paymentAccount.getAccountNo() == null
                    || PaymentMethod.fromCode(paymentAccount.getPaymentMethod()) == null) {
                throw new RuntimeException("Unvalid request");
            }
        } catch (Exception e) {
            log.warn("Unvalid request.", e);
            return null;
        }
        return paymentAccount;
    }
}
