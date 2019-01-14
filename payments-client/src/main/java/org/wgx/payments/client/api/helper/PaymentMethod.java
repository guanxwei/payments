package org.wgx.payments.client.api.helper;

/**
 * Payment method enum.
 */
public enum PaymentMethod {

    /**
     * Payment method credit card.
     */
    CREDIT_CARD("CreditCard", 10, false),

    /**
     * Payment method credit card.
     */
    DEBIT_CARD("DebitCard", 20, false),

    /**
     * Payment method accounts
     */
    ACCOUNTS("Accounts", 30, true),

    /**
     * Payment method points.
     */
    POINTS("Points", 40, true),

    /**
     * Payment method Gift card.
     */
    GIFT_CARD("GC", 50, true),

    /**
     * Payment method IAP. For reference, please check here: https://support.apple.com/en-us/HT202023.
     */
    IAP("IAP", 60, false),

    /**
     * Payment method Alipay.
     */
    ALIPAY("Alipay", 70, false),

    /**
     * Payment method Wechat.
     */
    WECHAT("Wechat", 80, false);

    // CHECKSTYLE:OFF
    private String paymentMethodName;
    private int paymentMethodCode;
    // 是否是平台内部支付方式，一个内部支付方式可以和一个非内部支付方式一起联合支付，其他任何情况不允许多种支付方式支付一笔请求.
    private boolean internal;

    private PaymentMethod(final String paymentMethodName, final int paymentMethodCode,
            final boolean internal) {
        this.paymentMethodName = paymentMethodName;
        this.paymentMethodCode = paymentMethodCode;
        this.internal = internal;
    }

    public String paymentMethodName() {
        return this.paymentMethodName;
    }

    public int paymentMethodCode() {
        return this.paymentMethodCode;
    }

    public boolean isInternal() {
        return this.internal;
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
