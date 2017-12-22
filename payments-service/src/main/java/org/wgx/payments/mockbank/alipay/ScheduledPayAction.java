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
public class ScheduledPayAction implements RoleAction {

    @Resource @Setter
    private AccountFactory accountFactory;

    private SignatureGenerator externalTransactionSignatureGenerator = SignatureGenerator.MIXED_20;

    /**
     * {@inheritDoc}
     */
    @Override
    public String act(final HttpServletRequest request) {
        String result = "<?xml version=\"1.0\" encoding=\"utf-8\"?><alipay><is_success>T</is_success><response><alipay>"
                + "<result_code>ORDER_SUCCESS_PAY_SUCCESS</result_code><detail_error_code>ORDER_SUCCESS_PAY_SUCCESS</detail_error_code>"
                + "<detail_error_des>交易买家不匹配</detail_error_des><trade_no>2013112311001004940000384027</trade_no>"
                + "</alipay></response></alipay>";
        Enumeration<String> keys = request.getParameterNames();
        SortedMap<String, String> inputParameters = new TreeMap<>();
        String inputSignature = "";
        boolean status = false;
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
            if (value.equals("5.00")) {
                status = false;
            } else if (value.equals("10.00")) {
                status = true;
            } else if (value.equals("6.00")) {
                return "<?xml version=\"1.0\" encoding=\"utf-8\"?><alipay><is_success>F</is_success><error>ILLEGAL_SIGN</error></alipay>";
            } else if (value.equals("7.00")) {
                result = "<?xml version=\"1.0\" encoding=\"utf-8\"?><alipay><is_success>T</is_success><response><alipay>"
                        + "<result_code>ORDER_FAIL</result_code><detail_error_code>USER_NOT_MATCH</detail_error_code>"
                        + "<detail_error_des>交易买家不匹配</detail_error_des><trade_no>2013112311001004940000384027</trade_no>"
                        + "</alipay></response></alipay>";
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
        String notifyID = externalTransactionSignatureGenerator.generate();
        SortedMap<String, String> contents = new TreeMap<>();
        contents.put("notify_id", notifyID);
        contents.put("out_trade_no", transactionID);
        contents.put("trade_no", SignatureGenerator.MIXED_20.generate());
        if (status) {
            contents.put("trade_status", "TRADE_SUCCESS");
        } else {
            contents.put("trade_status", "TRADE_FAIL");
        }
        String signature = AlipayUtils.generateSignature(contents, false, accountFactory.getPrivateKeyByMaterialName(materialName));
        contents.put("sign", signature);
        contents.put("sign_type", "RSA");
        Runnable worker = new NotifyRunner("http://localhost:8880/music/payments/callback/Alipay/scheduledpay", contents);
        EXECUTOR.submit(worker);
        return result;
    }

}
