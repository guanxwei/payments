package org.wgx.payments.mockbank.wechat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.mockbank.RoleAction;
import org.wgx.payments.mockbank.XMLNotifier;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.signature.SignatureGenerator;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.WechatSignatureHelper;
import org.wgx.payments.utils.XMLUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Alipay scheduled pay action mocker.
 *
 */
@Slf4j
public class ScheduledPayAction implements RoleAction {

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
        String appID = (String) parameters.get("appid");
        String account = null;
        for (String accountName : keyFactory.getWechatAccountList()) {
            if (keyFactory.getAccount(accountName).getPrivateKey().equals(appID)) {
                account = accountName;
                break;
            }
        }
        String content = prepareResponse((String)parameters.get("out_trade_no"));
        String notify = prepareNotifyResponse((String)parameters.get("out_trade_no"), account);
        Runnable worker = new XMLNotifier("http://localhost:8880/music/payments/callback/Wechat/scheduledpay", notify);
        EXECUTOR.submit(worker);
        return content;
    }

    private String prepareResponse(final String transactionID) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("return_code", "SUCCESS");
        parameters.put("return_msg", "SUCCESS");
        parameters.put("result_code", "SUCCESS");
        String accountName = keyFactory.getWechatAccountList().get(0);
        parameters.put("mch_id", keyFactory.getAccount(accountName).getAccountNo());
        parameters.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        parameters.put("contract_id", "wechatScheduledPayID");
        parameters.put("contract_code", transactionID);
        String materialName = keyFactory.getMaterialNameByAccountName(keyFactory.getWechatAccountList().get(0));
        parameters.put("appid", keyFactory.getAccount(accountName).getPrivateKey());
        String sign = WechatSignatureHelper.getSignWithKey(parameters, keyFactory.getPublicKeyByMaterialName(materialName));
        parameters.put("sign", sign);
        return XMLUtils.mapToXmlStr(parameters);
    }

    private String prepareNotifyResponse(final String transactionID, final String accountName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("attach", "微信支付");
        parameters.put("bank_type", "CFT");
        parameters.put("mch_id", keyFactory.getAccount(accountName).getAccountNo());
        parameters.put("fee_type", "CNY");
        parameters.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        parameters.put("result_code", "SUCCESS");
        parameters.put("is_subscribe", "n");
        parameters.put("openid", "oUpF8uMEb4qRXf22hE3X68TekukE");
        parameters.put("return_code", "SUCCESS");
        parameters.put("transaction_id", "wechatScheduledPayID");
        parameters.put("out_trade_no", transactionID);
        parameters.put("appid", keyFactory.getAccount(accountName).getPrivateKey());
        String sign = WechatSignatureHelper.getSignWithKey(parameters, keyFactory.getAccount(accountName).getPublicKey());
        parameters.put("sign", sign);
        return XMLUtils.mapToXmlStr(parameters);
    }
}
