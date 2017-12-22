package org.wgx.payments.client.api.helper;

/**
 * Payment method enum.
 */
public enum PaymentMethod {

    /**
     * Payment method Alipay.
     */
    ALIPAY("Alipay", 70),

    /**
     * Payment method Wechat.
     */
    WECHAT("Wechat", 80),

    /**
     * Payment method Netease.
     */
    @Deprecated
    NETEASE("Netease", 90),

    /**
     * Payment method IAP. For reference, please check here: https://support.apple.com/en-us/HT202023.
     */
    IAP("IAP", 60),

    /**
     * Payment method points.
     */
    POINTS("Points", 40),

    /**
     * Payment method accounts(零钱支付,用户的零钱将统一记账在云音乐电子账户里).
     */
    ACCOUNTS("Accounts", 30),

    /**
     * Payment method Gift card.
     */
    GIFT_CARD("GC", 50);

    // CHECKSTYLE:OFF
    private String paymentMethodName;
    private int paymentMethodCode;

    private PaymentMethod(final String paymentMethodName, final int paymentMethodCode) {
        this.paymentMethodName = paymentMethodName;
        this.paymentMethodCode = paymentMethodCode;
    }

    public String paymentMethodName() {
        return this.paymentMethodName;
    }

    public int paymentMethodCode() {
        return this.paymentMethodCode;
    }

    public static PaymentMethod fromCode(final int paymentMethodCode) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.paymentMethodCode == paymentMethodCode) {
                return method;
            }
        }
        return null;
    }

    public static PaymentMethod fromName(final String paymentMethodName) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.paymentMethodName.equals(paymentMethodName)) {
                return method;
            }
        }
        return null;
    }
    // CHECKSTYLE:ON

}
