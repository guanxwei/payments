package org.wgx.payments.validator;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.utils.WechatSignatureHelper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Wechat processor's validator.
 *
 */
@Slf4j
public class WechatValidator implements Validator<CreateOrUpdatePaymentResponseRequest> {

    @Setter @Resource
    private AccountFactory accountFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(final CreateOrUpdatePaymentResponseRequest request) {
        Map<String, Object> objects = new HashMap<>();
        for (String key : request.getParameters().keySet()) {
            objects.put(key, request.getParameters().get(key)[0]);
        }
        boolean succeed = false;
        for (String account : accountFactory.getWechatAccountList()) {
            succeed = WechatSignatureHelper.signVerifyWithKey(objects, accountFactory.getPublicKeyByMaterialName(
                    accountFactory.getMaterialNameByAccountName(account)));
            if (succeed) {
                log.info("Account [{}] used to verify the incoming request", account);
                 break;
            }
        }
        return succeed;
    }

}
