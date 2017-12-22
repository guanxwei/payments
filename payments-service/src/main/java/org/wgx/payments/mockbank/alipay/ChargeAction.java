package org.wgx.payments.mockbank.alipay;

import java.util.Enumeration;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.wgx.payments.mockbank.NotifyRunner;
import org.wgx.payments.mockbank.RoleAction;
import org.wgx.payments.signature.AccountFactory;
import org.wgx.payments.signature.SignatureGenerator;
import org.wgx.payments.utils.AlipayUtils;
import org.wgx.payments.utils.RSAUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Alipay charge action mocker.
 *
 */
@Slf4j
public class ChargeAction implements RoleAction {

    @Resource @Setter
    private AccountFactory accountFactory;

    private SignatureGenerator externalTransactionSignatureGenerator = SignatureGenerator.MIXED_20;

    /**
     * {@inheritDoc}
     */
    @Override
    public String act(final HttpServletRequest request) {
        String result = "success";
        Enumeration<String> keys = request.getParameterNames();
        SortedMap<String, String> inputParameters = new TreeMap<>();
        String inputSignature = "";
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            log.info("Alipay side received key:" + key);
            String value = request.getParameter(key);
            log.info("Alipay side received value:" + request.getParameter(key));
            if (key.equals("sign") || key.equals("sign_type")) {
                if (key.equals("sign")) {
                    inputSignature = value;
                }
            } else {
                inputParameters.put(key, value);
            }
        }
        String inputContent = AlipayUtils.createLinkString(inputParameters, false);
        String materialName = null;
        boolean verifyResult = false;
        for (String account : accountFactory.getAlipayAccountList()) {
            verifyResult = RSAUtils.verify(inputContent, inputSignature,
                    accountFactory.getPublicKeyByMaterialName(accountFactory.getMaterialNameByAccountName(account)), "utf-8");

            if (verifyResult) {
                materialName = accountFactory.getMaterialNameByAccountName(account);
                break;
            }
        }
        log.info("Alipay side verify result:" + verifyResult);
        // Call response handler url.
        String transactionID = request.getParameter("out_trade_no");
        String externalTransactionID = externalTransactionSignatureGenerator.generate();
        String notifyID = externalTransactionSignatureGenerator.generate();
        String tradeStatus = "TRADE_FINISHED";
        SortedMap<String, String> contents = new TreeMap<>();
        contents.put("trade_status", tradeStatus);
        contents.put("out_trade_no", transactionID);
        contents.put("trade_no", externalTransactionID);
        contents.put("notify_id", notifyID);
        String signature = AlipayUtils.generateSignature(contents, false, accountFactory.getPrivateKeyByMaterialName(materialName));
        contents.put("sign", signature);
        contents.put("sign_type", "RSA");
        log.info("Hello:" + AlipayUtils.createLinkString(contents, false));
        Runnable worker = new NotifyRunner("http://localhost:8880/music/payments/callback/Alipay/charge", contents);
        EXECUTOR.submit(worker);
        return result;
    }
}
