package org.wgx.payments.mockbank.wechat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.mockbank.RoleAction;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.signature.SignatureGenerator;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.WechatSignatureHelper;
import org.wgx.payments.utils.XMLUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Wechat charge action mocker.
 *
 */
@Slf4j
public class RescindAction implements RoleAction {

    @Resource @Setter
    private AccountFactory keyFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public String act(final HttpServletRequest request) {
        Map<String, Object> parameters = null;
        try {
            parameters = InputStreamHanlder.handle(request.getInputStream());
        } catch (IOException e) {
            log.error("Error request", e);
        }
        log.info("Wechat side receive cloud music request with data : [{}]", Jackson.json(parameters));
        return prepareResponse(parameters);
    }

    private String prepareResponse(final Map<String, Object> map) {
        Map<String, Object> parameters = new HashMap<>();
        String appID = (String) map.get("appid");
        String accountName = null;
        for (String account : keyFactory.getWechatAccountList()) {
            if (keyFactory.getAccount(account).getPrivateKey().equals(appID)) {
                accountName = account;
                break;
            }
        }
        parameters.put("return_code", "SUCCESS");
        parameters.put("return_msg", "SUCCESS");
        parameters.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        parameters.put("result_code", "SUCCESS");
        parameters.put("mch_id", keyFactory.getAccount(accountName).getAccountNo());
        parameters.put("appid", keyFactory.getAccount(accountName).getPrivateKey());
        String sign = WechatSignatureHelper.getSignWithKey(parameters, keyFactory.getAccount(accountName).getPublicKey());
        parameters.put("sign", sign);
        return XMLUtils.mapToXmlStr(parameters);
    }
}
