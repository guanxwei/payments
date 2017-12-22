package org.wgx.payments.deducer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.wgx.payments.client.api.helper.PaymentChannel;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.tools.Jackson;
import org.wgx.payments.utils.AccountUtils;
import org.wgx.payments.utils.WechatSignatureHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * Wechat used only deducer to help deduce return url from the Wechat returned response according to payment channel.
 *
 */
@Slf4j
public class WechatURLDeducer implements Deducer<Triple<CreatePaymentRequest, String, Map<String, Object>>, String> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String deduce(final Triple<CreatePaymentRequest, String, Map<String, Object>> input) {
        String qrurl = null;
        if (PaymentChannel.PC.channel().equals(input.getLeft().getChannel())) {
            // For reference, please check: https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=6_4
            qrurl = (String) input.getRight().get("code_url");
            log.info(String.format("New generated QR code url [%s] for reference [%s]: ", qrurl, input.getMiddle()));
        } else if (PaymentChannel.WAP.channel().equals(input.getLeft().getChannel())) {
            // For reference, please check: https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=7_7&index=6
            qrurl = (String) input.getRight().get("mweb_url");
            log.info(String.format("New generated MWEB url [%s] for reference [%s]: ", qrurl, input.getMiddle()));
        } else if (PaymentChannel.PUBLIC_ACCOUNT.channel().equals(input.getLeft().getChannel())) {
            // For reference, please check: https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=7_7&index=6
            qrurl = generatePublicAccountURL((String) input.getRight().get("prepay_id"));
            log.info(String.format("New generated prepay id [%s] for reference [%s]: ", qrurl, input.getMiddle()));
        } else if (PaymentChannel.MOBILE.channel().equals(input.getLeft().getChannel())) {
           // For reference, please check:https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_5
            qrurl = generateMobileURL((String) input.getRight().get("prepay_id"));
            log.info(String.format("New generated prepay id [%s] for reference [%s]: ", qrurl, input.getMiddle()));
        } else if (PaymentChannel.IPAD.channel().equals(input.getLeft().getChannel())) {
            // For reference, please check:https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_5
            qrurl = generateMobileURL((String) input.getRight().get("prepay_id"));
            log.info(String.format("New generated prepay id [%s] for reference [%s]: ", qrurl, input.getMiddle()));
        }
        return qrurl;
    }

    private Map<String, Object> prepare() {
        Long timestamp = System.currentTimeMillis() / 1000;
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("appid", AccountUtils.get().getPrivateKey());
        paramsMap.put("timestamp", timestamp.toString());
        paramsMap.put("noncestr", RandomStringUtils.randomAlphanumeric(32));

        return paramsMap;
    }

    private String generateMobileURL(final String prepayID) {
        Map<String, Object> paramsMap = prepare();
        paramsMap.put("partnerid", AccountUtils.get().getAccountNo());
        paramsMap.put("prepayid", prepayID);
        paramsMap.put("package", "Sign=WXPay");
        String sign = WechatSignatureHelper.getSignWithKey(paramsMap, AccountUtils.get().getPublicKey());
        paramsMap.put("sign", sign);
        String result = Jackson.json(paramsMap);
        return result;
    }

    private String generatePublicAccountURL(final String prepayID) {
        Map<String, Object> paramsMap = prepare();
        paramsMap.put("package", "prepay_id=" + prepayID);
        paramsMap.put("signType", "MD5");
        String sign = WechatSignatureHelper.getSignWithKey(paramsMap, AccountUtils.get().getPublicKey());
        paramsMap.put("paySign", sign);
        String result = Jackson.json(paramsMap);
        return result;
    }
}
