package org.wgx.payments.utils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.wgx.payments.signature.SignatureGenerator;

/**
 * Wechat utility class.
 *
 */
public final class WechatUtils {

    private WechatUtils() { }

    /**
     * Update 2017/07/28.
     *
     * Currently, this field is only used for H5 pay use case, Wechat's document specifies that
     * user's ip address is needed to generate payment url, but we are not going to modify the
     * original method signature in case other user cases(potential) may need other additional
     * parameters.
     */
    public static final ThreadLocal<Map<String, String>> ADDITIONAL = new ThreadLocal<>();

    /**
     * Build Wechat charge operation parameters.
     * @param transactionID Charge operation transaction ID.
     * @param amount Requested amount.
     * @param tradeType Trade type.
     * @param appID appID
     * @param body Wechat needed good body.
     * @param key Wechat assigned key.
     * @return Parameter map.
     */
    public static Map<String, Object> buildChargeParameters(final String transactionID, final String amount, final String tradeType,
            final String appID, final String body, final String key) {
        Map<String, Object> sortedMap = new HashMap<>();
        sortedMap.put("appid", appID);
        sortedMap.put("mch_id", AccountUtils.get().getAccountNo());
        sortedMap.put("device_info", "");
        sortedMap.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        sortedMap.put("body", body);
        sortedMap.put("detail", body);
        sortedMap.put("attach", AccountUtils.get().getAccountName());
        sortedMap.put("out_trade_no", transactionID);
        sortedMap.put("fee_type", "CNY");
        sortedMap.put("total_fee", convertFee(amount));
        sortedMap.put("spbill_create_ip", "127.0.0.1");
        if ("MWEB".contentEquals(tradeType)) {
            sortedMap.put("spbill_create_ip", ADDITIONAL.get().get("user_ip"));
        }
        Date date = new Date();
        sortedMap.put("time_start", new SimpleDateFormat("yyyyMMddHHmmss").format(date));
        long time = date.getTime() + 1800000;
        date = new Date(time);
        sortedMap.put("time_expire", new SimpleDateFormat("yyyyMMddHHmmss").format(date));
        sortedMap.put("goods_tag", "");
        sortedMap.put("notify_url", WechatConstants.CHARGE_NOTIFY_URL);
        sortedMap.put("trade_type", tradeType);
        sortedMap.put("product_id", "");
        sortedMap.put("limit_pay", "");
        sortedMap.put("openid", "");
        if ("JSAPI".equals(tradeType)) {
            sortedMap.put("openid", ADDITIONAL.get().get("openid"));
        }
        ADDITIONAL.set(null);
        String sign = WechatSignatureHelper.getSignWithKey(sortedMap, key);
        sortedMap.put("sign", sign);
        return sortedMap;
    }

    /**
     * Build sign operation needed URL.
     * @param transactionID Sign request transaction id.
     * @param userID User id to be showed in Wechat's sign page.
     * @param key Wechat assigned key.
     * @return Sign URL.
     */
    public static String buildSignURL(final String transactionID, final String userID, final String key) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", AccountUtils.get().getPrivateKey());
        params.put("mch_id", AccountUtils.get().getAccountNo());
        params.put("notify_url", WechatConstants.SIGN_NOTIFY_URL);
        params.put("plan_id", "42203");
        params.put("contract_code", transactionID);
        params.put("request_serial", transactionID);
        params.put("contract_display_account", userID);
        params.put("version", "1.0");
        params.put("timestamp", Long.toString(System.currentTimeMillis()));
        params.put("outerid", userID);
        String sign = WechatSignatureHelper.getSignWithKey(params, key);
        params.put("sign", sign);
        StringBuilder sb = new StringBuilder();
        sb.append(WechatConstants.ENTRUST_WEB_API).append("?");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!"".equals(entry.getValue())) {
                if (entry.getKey().equals("notify_url")) {
                    try {
                        sb.append(entry.getKey())
                          .append("=")
                          .append(URLEncoder.encode(String.valueOf(entry.getValue()), "utf-8"))
                          .append("&");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Build rescind request needed string.
     * @param externalTransactionID Wechat sent back transaction id for previous sign request.
     * @param key Wechat assigned key.
     * @return Rescind request needed XML style string.
     */
    public static String buildRescindXMLString(final String externalTransactionID, final String key) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", AccountUtils.get().getPrivateKey());
        params.put("mch_id", AccountUtils.get().getAccountNo());
        params.put("contract_id", externalTransactionID);
        params.put("contract_termination_remark", "RESCIND_CONTRACT");
        params.put("version", "1.0");
        String sign = WechatSignatureHelper.getSignWithKey(params, key);
        params.put("sign", sign);
        return XMLUtils.mapToXmlStr(params);
    }

    /**
     * Build refund request needed string.
     * @param appID APP ID.
     * @param chargeTransactionID Previous charge request's transaction ID.
     * @param refundTransactionID Refund request's transaction ID.
     * @param requestedAmount Charge amount.
     * @param refundAmount Refund amount.
     * @param key Wechat assigned key.
     * @return XML style string.
     */
    public static String buildRefundXMLString(final String appID, final String chargeTransactionID,
            final String refundTransactionID, final String requestedAmount, final String refundAmount, final String key) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", appID);
        params.put("mch_id", AccountUtils.get().getAccountNo());
        params.put("device_info", "");
        params.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        params.put("out_trade_no", chargeTransactionID);
        params.put("out_refund_no", refundTransactionID);
        params.put("total_fee", convertFee(requestedAmount));
        params.put("refund_fee", convertFee(refundAmount));
        params.put("refund_fee_type", "CNY");
        params.put("op_user_id", AccountUtils.get().getAccountNo());
        String sign = WechatSignatureHelper.getSignWithKey(params, key);
        params.put("sign", sign);
        return XMLUtils.mapToXmlStr(params);
    }

    /**
     * Build scheduled pay needed XML string.
     * @param transactionID Scheduled pay request's transaction ID.
     * @param amount Requested amount.
     * @param contractID Sign contract id returned by Wechat.
     * @param business Business name shown in Wechat APP charge page.
     * @param key Wechat assigned key.
     * @return Scheduled pay request XML String.
     */
    public static String buildScheduledPayXMLString(final String transactionID, final String amount,
            final String contractID, final String business, final String key) {
        Map<String, Object> params = new HashMap<>();
        params.put("body", business);
        params.put("detail", WechatConstants.BODY);
        params.put("out_trade_no", transactionID);
        params.put("fee_type", "CNY");
        params.put("total_fee", convertFee(amount));
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("time_start", getCurrentTime());
        params.put("notify_url", WechatConstants.SCHEDULED_PAY_NOTIFY_URL);
        params.put("trade_type", "PAP");
        params.put("contract_id", contractID);
        params.put("attach", "fda");
        params.put("appid", AccountUtils.get().getPrivateKey());
        params.put("mch_id", AccountUtils.get().getAccountNo());
        params.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        String sign = WechatSignatureHelper.getSignWithKey(params, key);
        params.put("sign", sign);
        return XMLUtils.mapToXmlStr(params);
    }

    /**
     * Build Wechat query refund needed XML String.
     * @param refundTransactionID Refund request's transaction ID.
     * @param appID The APP ID assigned by Wechat.
     * @param key Wechat assigned key.
     * @return Query refund request XML String.
     */
    public static String buildQueryRefundXMLString(final String refundTransactionID, final String appID, final String key) {
        Map<String, Object> params = new HashMap<>();
        params.put("appid", appID);
        params.put("mch_id", AccountUtils.get().getAccountNo());
        params.put("nonce_str", SignatureGenerator.ALPHA_20.generate());
        params.put("out_refund_no", refundTransactionID);
        String sign = WechatSignatureHelper.getSignWithKey(params, key);
        params.put("sign", sign);
        return XMLUtils.mapToXmlStr(params);
    }

    private static String getCurrentTime() {
        Date now = new Date();
        return new SimpleDateFormat("yyyyMMddHHmmss").format(now);
    }

    private static String convertFee(final String totalFee) {
        BigDecimal fee = new BigDecimal(totalFee);
        BigDecimal total = fee.multiply(new BigDecimal(100));
        return total.toBigInteger().toString();
    }

}
