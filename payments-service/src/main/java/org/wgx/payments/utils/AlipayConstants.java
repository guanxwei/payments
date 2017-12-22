package org.wgx.payments.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.wgx.payments.client.api.helper.PaymentMethod;
import org.wgx.payments.client.api.helper.PaymentOperation;

/**
 * Alipay related constants holder.
 *
 */
public final class AlipayConstants {

    // CHECKSTYLE:OFF

    /**
     * Charset.
     */
    public static final String INPUT_CHARSET = "utf-8";

    /**
     * Seller email.
     */
    public static final String SELLER_EMAIAL = "****";

    /**
     * Subject.
     */
    public static final String SUBJECT = "CloudMusicOrder";

    /**
     * Payment type.
     */
    public static final String PAYMENT_TYPE = "1";

    /**
     * PC channel service parameter.
     */
    public static final String DIRECT_PAY_SERVICE = "create_direct_pay_by_user";

    /**
     * Mobile(APP) channel service parameter.
     */
    public static final String MOBILE_PAY_SERVICE = "mobile.securitypay.pay";

    /**
     * VIP auto charge sign service parameter.
     */
    public static final String SING_PAGE_SERVICE = "alipay.dut.customer.agreement.page.sign";

    /**
     * VIP auto charge rescind service parameter.
     */
    public static final String RESCIND_PAGE_SERVICE = "alipay.dut.customer.agreement.unsign";

    /**
     * Checkbook download query service parameter.
     */
    public static final String DOWNLOAD_BILL_SERVICE = "alipay.data.dataservice.bill.downloadurl.query";

    /**
     * Alipay notify_id verification url.
     */
    public static final String HTTPS_VERIFY_URL;

    /**
     * Alipay gateway url.
     */
    public static final String GATEWAY_URL;

    /**
     * Signature algotithm.
     */
    public static final String SIGN_TYPE = "RSA";

    /**
     * Mobile(browser) channel service parameter.
     */
    public static final String WAP_PAY_SERVICE = "alipay.wap.create.direct.pay.by.user";

    /**
     * Charge operation notify url.
     */
    public static final String CHARGE_NOTIFY_URL = Constants.BASE_URL + "/"
            + PaymentMethod.ALIPAY.paymentMethodName() + "/" + PaymentOperation.CHARGE.operationType();

    /**
     * Refund operation notify url.
     */
    public static final String REFUND_NOTIFY_URL = Constants.BASE_URL + "/"
            + PaymentMethod.ALIPAY.paymentMethodName() + "/" + PaymentOperation.REFUND.operationType();

    /**
     * Sign operation notify url.
     */
    public static final String SIGN_NOTIFY_URL = Constants.BASE_URL + "/"
            + PaymentMethod.ALIPAY.paymentMethodName() + "/" + PaymentOperation.SIGN.operationType();

    /**
     * Rescind operation notify url.
     */
    public static final String RESCIND_NOTIFY_URL = Constants.BASE_URL + "/"
            + PaymentMethod.ALIPAY.paymentMethodName() + "/" + PaymentOperation.RESCIND.operationType();

    /**
     * ScheduledPay operation notify url.
     */
    public static final String SCHEDULED_PAY_NOTIFY_URL = Constants.BASE_URL + "/"
            + PaymentMethod.ALIPAY.paymentMethodName() + "/" + PaymentOperation.SCHEDULEDPAY.operationType();

    private AlipayConstants() { }

    static {
        String profile = System.getProperty("spring.profiles.active");
        Properties properties = new Properties();
        try (InputStream input = Constants.class.getResourceAsStream(String.format("/payments-server-%s.properties", profile))) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HTTPS_VERIFY_URL = properties.getProperty("alipay.verify.url");
        GATEWAY_URL = properties.getProperty("alipay.gateway.url");
    }
    // CHECKSTYLE:ON
}
