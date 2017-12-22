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
 * Alipay charge action mocker.
 *
 */
@Slf4j
public class ChargeAction implements RoleAction {

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
        String amount = (String) parameters.get("total_fee");
        String content = null;
        boolean isMobile = !"NATIVE".equals(parameters.get("trade_type"));
        String accountName = (String) parameters.get("attach");
        if (amount.equals("500")) {
            content = prepareResponse("FAIL", "FAIL", isMobile, accountName);
        } else if (amount.equals("600")) {
            content = prepareResponse("SUCCESS", "FAIL", isMobile, accountName);
        } else {
            content = prepareResponse("SUCCESS", "SUCCESS", isMobile, accountName);
            String notify = prepareNotifyResponse((String)parameters.get("out_trade_no"), accountName);
            Runnable worker = new XMLNotifier("http://localhost:8880/music/payments/callback/Wechat/charge", notify);
            EXECUTOR.submit(worker);
        }
        return content;
    }

    private String prepareResponse(final String returnCode, final String resultCode, final boolean isMobile, final String accountName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("return_code", returnCode);
        parameters.put("return_msg", "SUCCESS");

        parameters.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        parameters.put("result_code", returnCode);
        if (isMobile) {
            parameters.put("prepay_id", "prepayid");
        } else {
            parameters.put("code_url", "wexin:pay?id=1234567");
        }
        if (resultCode == "FAIL") {
            parameters.put("err_code_des", "errorCode");
        }
        parameters.put("mch_id", keyFactory.getAccount(accountName).getAccountNo());
        parameters.put("appid", keyFactory.getAccount(accountName).getPrivateKey());
        String sign = WechatSignatureHelper.getSignWithKey(parameters, keyFactory.getAccount(accountName).getPublicKey());
        parameters.put("sign", sign);
        return XMLUtils.mapToXmlStr(parameters);
    }

    private String prepareNotifyResponse(final String transactionID, final String accountName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("attach", "微信支付");
        parameters.put("bank_type", "CFT");
        parameters.put("fee_type", "CNY");
        parameters.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        parameters.put("result_code", "SUCCESS");
        parameters.put("is_subscribe", "n");
        parameters.put("openid", "oUpF8uMEb4qRXf22hE3X68TekukE");
        parameters.put("return_code", "SUCCESS");
        parameters.put("transaction_id", "1004400740201409030005092168");
        parameters.put("out_trade_no", transactionID);
        parameters.put("mch_id", keyFactory.getAccount(accountName).getAccountNo());
        parameters.put("appid", keyFactory.getAccount(accountName).getPrivateKey());
        String sign = WechatSignatureHelper.getSignWithKey(parameters, keyFactory.getAccount(accountName).getPublicKey());
        parameters.put("sign", sign);
        return XMLUtils.mapToXmlStr(parameters);
    }
}
