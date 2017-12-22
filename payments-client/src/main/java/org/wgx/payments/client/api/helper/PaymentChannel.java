package org.wgx.payments.client.api.helper;

/**
 * Payment channels.
 */
public enum PaymentChannel {

    /**
     * PC channel.
     */
    PC("PC"),

    /**
     * Mobile phone channel(APP), not including IPAD.
     */
    MOBILE("MOBILE"),

    /**
     * Mobile browser channel.
     */
    WAP("WAP"),

    /**
     * Cloud music IPAD channel. IPAD channel is kind of special MOBILE.
     */
    IPAD("IPAD"),

    /**
     * Public account, currently mainly reserved for Wechat public account pay.
     */
    PUBLIC_ACCOUNT("PUBACC");

    // CHECKSTYLE:OFF
    private String channel;

    private PaymentChannel(final String channel) {
        this.channel = channel;
    }

    public String channel() {
        return this.channel;
    }
    // CHECKSTYLE:OFF
}
