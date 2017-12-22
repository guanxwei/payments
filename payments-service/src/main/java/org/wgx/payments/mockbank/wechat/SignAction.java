package org.wgx.payments.mockbank.wechat;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.mockbank.RoleAction;
import org.wgx.payments.mockbank.XMLNotifier;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.signature.SignatureGenerator;
import org.wgx.payments.utils.WechatSignatureHelper;
import org.wgx.payments.utils.XMLUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Alipay charge action mocker.
 *
 */
@Slf4j
public class SignAction implements RoleAction {

    @Resource @Setter
    private AccountFactory keyFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public String act(final HttpServletRequest request) {
        log.info("Wechat side receive cloud music request with data : [{}]", request.getQueryString());
        String content = null;
        String appID = request.getParameter("appid");
        String account = null;
        for (String accountName : keyFactory.getWechatAccountList()) {
            if (keyFactory.getAccount(accountName).getPrivateKey().equals(appID)) {
                account = accountName;
                break;
            }
        }
        String notify = prepareResponse(request.getParameter("contract_code"), account);
        Runnable worker = new XMLNotifier("http://localhost:8880/music/payments/callback/Wechat/sign", notify);
        EXECUTOR.submit(worker);
        return content;
    }

    private String prepareResponse(final String transactionID, final String accountName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("return_code", "SUCCESS");
        parameters.put("return_msg", "SUCCESS");
        parameters.put("result_code", "SUCCESS");
        parameters.put("mch_id", keyFactory.getAccount(accountName).getAccountNo());
        parameters.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        parameters.put("contract_id", "wechatSignID");
        parameters.put("contract_code", transactionID);
        parameters.put("appid", keyFactory.getAccount(accountName).getPrivateKey());
        String sign = WechatSignatureHelper.getSignWithKey(parameters, keyFactory.getAccount(accountName).getPublicKey());
        parameters.put("sign", sign);
        return XMLUtils.mapToXmlStr(parameters);
    }
}
