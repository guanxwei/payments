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
 * Alipay charge action mocker.
 *
 */
@Slf4j
public class RefundAction implements RoleAction {

    @Resource @Setter
    private AccountFactory keyFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public String act(final HttpServletRequest request) {
        Map<String, Object> parameters = null;
        String content = null;
        try {
            parameters = InputStreamHanlder.handle(request.getInputStream());
            log.info("Wechat side receive cloud music request with data : [{}]", Jackson.json(parameters));
            if (request.getRequestURI().contains("queryrefund")) {
                return prepareQueryRefundResponse();
            }
            String amount = (String) parameters.get("refund_fee");
            String outTradeNo = (String) parameters.get("out_trade_no");
            String refundNo = (String) parameters.get("out_refund_no");
            String appID = (String)parameters.get("appid");
            String account = null;
            for (String accountName : keyFactory.getWechatAccountList()) {
                if (appID.equals(keyFactory.getAccount(accountName).getPrivateKey())) {
                    account = accountName;
                    break;
                }
            }
            if (amount.equals("500")) {
                content = prepareResponse("FAIL", "FAIL", outTradeNo, refundNo, account);
            } else if (amount.equals("700")) {
                return "hello";
            } else if (amount.equals("600")) {
                content = prepareResponse("SUCCESS", "FAIL", outTradeNo, refundNo, account);
            } else {
                content = prepareResponse("SUCCESS", "SUCCESS", outTradeNo, refundNo, account);
            }
        } catch (IOException e) {
            log.error("Error request", e);
        }
        return content;
    }

    private String prepareResponse(final String returnCode, final String resultCode,
            final String outTradeNo, final String refundNo, final String accountName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("return_code", returnCode);
        parameters.put("return_msg", "SUCCESS");
        parameters.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        parameters.put("result_code", resultCode);
        if (resultCode == "FAIL") {
            parameters.put("err_code_des", "errorCode");
        }
        parameters.put("out_trade_no", outTradeNo);
        parameters.put("out_refund_no", refundNo);
        parameters.put("refund_id", "testRefundID");
        parameters.put("mch_id", keyFactory.getAccount(accountName).getAccountNo());
        parameters.put("appid", keyFactory.getAccount(accountName).getPrivateKey());
        String sign = WechatSignatureHelper.getSignWithKey(parameters, keyFactory.getAccount(accountName).getPublicKey());
        parameters.put("sign", sign);
        return XMLUtils.mapToXmlStr(parameters);
    }

    private String prepareQueryRefundResponse() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("return_code", "SUCCESS");
        parameters.put("refund_status_0", "SUCCESS");
        parameters.put("refund_id_0", SignatureGenerator.ALPHA_20.generate());
        String materialName = keyFactory.getMaterialNameByAccountName(keyFactory.getWechatAccountList().get(0));
        String sign = WechatSignatureHelper.getSignWithKey(parameters, keyFactory.getPublicKeyByMaterialName(materialName));
        parameters.put("sign", sign);
        return XMLUtils.mapToXmlStr(parameters);
    }
}
