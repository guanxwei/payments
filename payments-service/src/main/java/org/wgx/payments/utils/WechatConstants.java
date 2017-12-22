package org.wgx.payments.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;

/**
 * Wechat related constants holder.
 *
 */
public final class WechatConstants {

    // CHECKSTYLE:OFF
    private WechatConstants() { }

    /**
     * Charge operation notify url.
     */
    public static final String CHARGE_NOTIFY_URL = Constants.BASE_URL + "/"
            + PaymentMethod.WECHAT.paymentMethodName() + "/" + PaymentOperation.CHARGE.operationType();

    /**
     * Refund operation notify url.
     */
    public static final String REFUND_NOTIFY_URL = Constants.BASE_URL + "/"
            + PaymentMethod.WECHAT.paymentMethodName() + "/" + PaymentOperation.REFUND.operationType();

    /**
     * Sign operation notify url.
     */
    public static final String SIGN_NOTIFY_URL = Constants.BASE_URL + "/"
            + PaymentMethod.WECHAT.paymentMethodName() + "/" + PaymentOperation.SIGN.operationType();

    /**
     * Rescind operation notify url.
     */
    public static final String RESCIND_NOTIFY_URL = Constants.BASE_URL + "/"
            + PaymentMethod.WECHAT.paymentMethodName() + "/" + PaymentOperation.RESCIND.operationType();

    /**
     * ScheduledPay operation notify url.
     */
    public static final String SCHEDULED_PAY_NOTIFY_URL = Constants.BASE_URL + "/"
            + PaymentMethod.WECHAT.paymentMethodName() + "/" + PaymentOperation.SCHEDULEDPAY.operationType();

    /**
     * Body parameter sent to Wechat.
     */
    public static final String BODY = "网易云音乐订单";

    /**
     * URL to generate QR code link.
     */
    public static final String CHARGE_API;

    /**
     * URL to sign contract with customer using Wechat.
     */
    public static final String ENTRUST_WEB_API;

    /**
     * URL to rescind contract with Wechat.
     */
    public static final String DELETE_CONTRACT_API;

    /**
     * Wechat refund api URL.
     */
    public static final String REFUND_API;

    /**
     * Wechat scheduled pay api URL.
     */
    public static final String PAP_PAY_APPLY_API;

    /**
     * Wechat query refund api.
     */
    public static final String QUERY_REFUND_API;

    /**
     * Wechat download checkbook API.
     */
    public static final String DOWNLOAD_BILL_API;

    static {
        String profile = System.getProperty("spring.profiles.active");
        Properties properties = new Properties();
        try (InputStream input = Constants.class.getResourceAsStream(String.format("/payments-server-%s.properties", profile))) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CHARGE_API = properties.getProperty("wechat.qr.pay.url");
        ENTRUST_WEB_API = properties.getProperty("wechat.sign.url");
        DELETE_CONTRACT_API = properties.getProperty("wechat.rescind.url");
        REFUND_API = properties.getProperty("wechat.refund.url");
        PAP_PAY_APPLY_API = properties.getProperty("wechat.scheduledpay.url");
        QUERY_REFUND_API = properties.getProperty("wechat.refund.query.url");
        DOWNLOAD_BILL_API = properties.getProperty("wechat.checkbook.download.url");
    }
    // CHECKSTYLE:ON
}
