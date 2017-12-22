package org.wgx.payments.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.wgx.payments.client.api.helper.PaymentChannel;
import org.wgx.payments.client.api.io.CreatePaymentRequest;
import org.wgx.payments.exception.ExceptionCode;
import org.wgx.payments.exception.PaymentsException;
import org.wgx.payments.tools.Jackson;

import com.google.common.collect.ImmutableMap;

/**
 * Alipay related util methods.
 *
 */
public final class AlipayUtils {

    private static final String PARTNER = "partner";
    private static final String INPUT_CHARSET = "_input_charset";
    private static final String SERVICE = "service";
    private static final String NOTIFY_URL = "notify_url";

    private static final ImmutableMap<String, String> CHANNEL_SERVICE_MAPPING = ImmutableMap.<String, String>builder()
            .put(PaymentChannel.PC.channel(), AlipayConstants.DIRECT_PAY_SERVICE)
            .put(PaymentChannel.MOBILE.channel(), AlipayConstants.MOBILE_PAY_SERVICE)
            .put(PaymentChannel.WAP.channel(), AlipayConstants.WAP_PAY_SERVICE)
            .put(PaymentChannel.IPAD.channel(), AlipayConstants.MOBILE_PAY_SERVICE)
            .build();

    private AlipayUtils() { }

    /**
     * Build key-value pairs per request.
     * @param request Create payment request.
     * @return Sorted map.
     */
    public static SortedMap<String, String> buildParameterMap(final CreatePaymentRequest request) {
        SortedMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put(PARTNER, AccountUtils.get().getAccountNo());
        sortedMap.put(INPUT_CHARSET, AlipayConstants.INPUT_CHARSET);
        if (request.getSpecialTags() != null && request.getSpecialTags().containsKey("client")) {
            String client = request.getSpecialTags().get("client");
            String version = request.getSpecialTags().get("version");
            String appEnv = null;
            if ("android".equals(client)) {
                appEnv = "system=android^version=" + version;
                sortedMap.put("app_id", "android");
            } else if ("iphone".equals(client)) {
                appEnv = "system=iphone^version=" + version;
                sortedMap.put("app_id", "iphone");
            }
            if (StringUtils.isNotBlank(appEnv)) {
                sortedMap.put("appenv", appEnv);
            }
        }
        sortedMap.put("seller_email", AlipayConstants.SELLER_EMAIAL);
        sortedMap.put("seller_user_id", AccountUtils.get().getAccountNo());
        sortedMap.put(NOTIFY_URL, AlipayConstants.CHARGE_NOTIFY_URL);
        sortedMap.put(SERVICE, CHANNEL_SERVICE_MAPPING.get(request.getChannel()));
        sortedMap.put("subject", AlipayConstants.SUBJECT);
        sortedMap.put("payment_type", AlipayConstants.PAYMENT_TYPE);
        sortedMap.put("seller_id", AccountUtils.get().getAccountNo());
        sortedMap.put("total_fee", request.getReferences().values().iterator().next());
        sortedMap.put("body", AlipayConstants.SUBJECT);
        sortedMap.put("it_b_pay", "30m");
        return sortedMap;
    }

    /**
     * Build the query string portion of the payment direct url.
     * @param sortedMap Sorted map containing parameter need to be built into query string.
     * @param isMobile Tag indicating if the request's payment channel is mobile.
     * @param privateKey Private key to generate signature.
     * @return Query string.
     */
    public static String buildRequestParaStr(final SortedMap<String, String> sortedMap, final boolean isMobile, final String privateKey) {
        String signature = generateSignature(sortedMap, isMobile, privateKey);
        sortedMap.put("sign", signature);
        sortedMap.put("sign_type", AlipayConstants.SIGN_TYPE);
        String queryString = createLinkString(sortedMap, isMobile);
        if (!isMobile) {
            try {
                for (Entry<String, String> entry : sortedMap.entrySet()) {
                    sortedMap.put(entry.getKey(), URLEncoder.encode(entry.getValue(), AlipayConstants.INPUT_CHARSET));
                }
                queryString = createLinkString(sortedMap, false);
            } catch (UnsupportedEncodingException e) {
                throw new PaymentsException(ExceptionCode.WRONG_PARAMETER, "Illegal parameters", e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return queryString;
    }

    /**
     * Generate signature based on input parameters.
     * @param sortedMap Key value pairs.
     * @param isMobile Tag.
     * @param privateKey Private key to generate signature.
     * @return Signature.
     */
    public static String generateSignature(final SortedMap<String, String> sortedMap, final boolean isMobile, final String privateKey) {
        String content = createLinkString(sortedMap, isMobile);
        return RSAUtils.sign(content, privateKey, AlipayConstants.INPUT_CHARSET);
    }

    /**
     * Create query string style string based on the input parameter map.
     * @param sortedMap Key-value pairs.
     * @param isMobile Tag indicating the client type.
     * @return Query string style string.
     */
    public static String createLinkString(final SortedMap<String, String> sortedMap, final boolean isMobile) {
        StringBuilder result = new StringBuilder();
        for (Entry<String, String> entry : sortedMap.entrySet()) {
            result.append(entry.getKey());
            result.append("=");
            if (isMobile) {
                result.append("\"");
            }
            if (isMobile && entry.getKey().equals("sign")) {
                try {
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                result.append(entry.getValue());
            }
            if (isMobile) {
                result.append("\"");
            }
            result.append("&");
        }

        return result.substring(0, result.length() - 1);
    }

    /**
     * Generate Alipay refund needed parameter map without signature related information.
     * @param externalTransactionID Alipay's External TransactionID saved when the charge operation's response returned.
     * @param refundTransactionID The refund request's transacionID.
     * @param amount Amount needs to be refund.
     * @return Sorted map contains all the parameters that needs to be used to generate signature.
     */
    public static SortedMap<String, String> generateRefundParameters(final String externalTransactionID,
            final String refundTransactionID, final String amount) {
        SimpleDateFormat formate = new SimpleDateFormat("yyyyMMdd");
        SortedMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put(SERVICE, "refund_fastpay_by_platform_nopwd");
        sortedMap.put(PARTNER, AccountUtils.get().getAccountNo());
        sortedMap.put(INPUT_CHARSET, AlipayConstants.INPUT_CHARSET);
        sortedMap.put(NOTIFY_URL, AlipayConstants.REFUND_NOTIFY_URL);
        sortedMap.put("seller_email", AlipayConstants.SELLER_EMAIAL);
        sortedMap.put("seller_user_id", AccountUtils.get().getAccountNo());
        sortedMap.put("refund_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
        String batchNum = "1";
        sortedMap.put("batch_no", formate.format(new Date()) + refundTransactionID);
        sortedMap.put("batch_num", batchNum);
        String detailData = StringUtils.join(new String[] {externalTransactionID, amount, "Refund"}, "^");
        sortedMap.put("detail_data", detailData);
        return sortedMap;
    }

    /**
     * Generate Alipay sign needed parameter map without signature related information.
     * @param signTransactionID The refund request's transacionID.
     * @param returnURL Return url.
     * @param channel Payment channel.
     * @return Sorted map contains all the parameters that needs to be used to generate signature.
     */
    public static SortedMap<String, String> generateSignParameters(final String signTransactionID,
            final String returnURL, final String channel) {
        SortedMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put(SERVICE, AlipayConstants.SING_PAGE_SERVICE);
        sortedMap.put(PARTNER, AccountUtils.get().getAccountNo());
        sortedMap.put(INPUT_CHARSET, AlipayConstants.INPUT_CHARSET);
        sortedMap.put(NOTIFY_URL, AlipayConstants.SIGN_NOTIFY_URL);
        if (StringUtils.isNotBlank(returnURL)) {
            sortedMap.put("return_url", returnURL);
        }
        sortedMap.put("product_code", "GENERAL_WITHHOLDING_P");
        sortedMap.put("external_sign_no", signTransactionID);
        sortedMap.put("scene", "INDUSTRY|DIGITAL_MEDIA");
        sortedMap.put("sign_validity_period", "30m");
        Map<String, Object> accessInfo = new HashMap<>();
        if (channel.equals(PaymentChannel.MOBILE.channel())) {
            accessInfo.put("channel", "ALIPAYAPP");
        } else {
            accessInfo.put("channel", channel);
        }
        sortedMap.put("access_info", Jackson.json(accessInfo));
        return sortedMap;
    }

    /**
     * Generate Alipay rescind needed parameter map without signature related information.
     * @param signTransactionID The rescind request's transacionID.
     * @param alipayUserID External transaction ID returned by Alipay when the customer signed contract with Cloud music.
     * @return Sorted map contains all the parameters that needs to be used to generate signature.
     */
    public static SortedMap<String, String> generateRescindParameters(final String signTransactionID, final String alipayUserID) {
        SortedMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put(SERVICE, AlipayConstants.RESCIND_PAGE_SERVICE);
        sortedMap.put(PARTNER, AccountUtils.get().getAccountNo());
        sortedMap.put(INPUT_CHARSET, AlipayConstants.INPUT_CHARSET);
        sortedMap.put(NOTIFY_URL, AlipayConstants.RESCIND_NOTIFY_URL);
        sortedMap.put("product_code", "GENERAL_WITHHOLDING_P");
        sortedMap.put("external_sign_no", signTransactionID);
        sortedMap.put("alipay_user_id", alipayUserID);
        sortedMap.put("scene", "INDUSTRY|DIGITAL_MEDIA");
        return sortedMap;
    }

    /**
     * Generate Alipay Scheduled pay parameter map without signature related information.
     * @param transactionID Scheduled pay request's transaction ID.
     * @param amount Requested amount.
     * @param agreementNo Agreement no. returned when customer sign contract with cloud music.
     * @return Sorted map.
     */
    public static SortedMap<String, String> generateScheduledPayParameter(final String transactionID,
            final String amount, final String agreementNo) {
        SortedMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put(SERVICE, "alipay.acquire.createandpay");
        sortedMap.put(PARTNER, AccountUtils.get().getAccountNo());
        sortedMap.put(INPUT_CHARSET, AlipayConstants.INPUT_CHARSET);
        sortedMap.put(NOTIFY_URL, AlipayConstants.SCHEDULED_PAY_NOTIFY_URL);
        sortedMap.put("out_trade_no", transactionID);
        sortedMap.put("subject", AlipayConstants.SUBJECT);
        sortedMap.put("body", AlipayConstants.SUBJECT);
        sortedMap.put("total_fee", amount);
        sortedMap.put("product_code", "GENERAL_WITHHOLDING");
        Map<String, String> agreementInfo = new HashMap<>();
        agreementInfo.put("agreement_no", agreementNo);
        sortedMap.put("agreement_info", Jackson.json(agreementInfo));
        return sortedMap;
    }
}
