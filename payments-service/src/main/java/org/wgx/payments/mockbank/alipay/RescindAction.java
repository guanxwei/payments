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
public class RescindAction implements RoleAction {

    @Resource @Setter
    private AccountFactory accountFactory;

    private SignatureGenerator externalTransactionSignatureGenerator = SignatureGenerator.MIXED_20;

    private static final String TEST_ALIPAY_USER_ID = "testAliapyUserID";
    private static final String AGREEMENT_NO = "testAgreementNo";

    /**
     * {@inheritDoc}
     */
    @Override
    public String act(final HttpServletRequest request) {
        String result = "<?xml version=\"1.0\" encoding=\"utf-8\"?><alipay><is_success>T</is_success></alipay>";
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
        String transactionID = request.getParameter("external_sign_no");
        String notifyID = externalTransactionSignatureGenerator.generate();
        String status = "UNSIGN";
        SortedMap<String, String> contents = new TreeMap<>();
        contents.put("agreement_no", AGREEMENT_NO);
        contents.put("status", status);
        contents.put("alipay_user_id", TEST_ALIPAY_USER_ID);
        contents.put("notify_id", notifyID);
        contents.put("external_sign_no", transactionID);
        String signature = AlipayUtils.generateSignature(contents, false, accountFactory.getPrivateKeyByMaterialName(materialName));
        contents.put("sign", signature);
        contents.put("sign_type", "RSA");
        Runnable worker = new NotifyRunner("http://localhost:8880/music/payments/callback/Alipay/rescind", contents);
        EXECUTOR.submit(worker);
        return result;
    }

}
