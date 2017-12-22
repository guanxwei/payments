package org.wgx.payments.deducer;

import org.wgx.payments.client.api.helper.PaymentChannel;
import org.wgx.payments.client.api.io.CreatePaymentRequest;

/**
 * Wechat trade type deducer to deduce trade type for charge operation.
 *
 */
public class WechatTradeTypeDeducer implements Deducer<CreatePaymentRequest, String> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String deduce(final CreatePaymentRequest request) {
        String tradeType;
        if (PaymentChannel.PC.channel().equals(request.getChannel())) {
            tradeType = "NATIVE";
        } else if (PaymentChannel.WAP.channel().equals(request.getChannel())) {
            tradeType = "MWEB";
        } else if (PaymentChannel.PUBLIC_ACCOUNT.channel().equals(request.getChannel())) {
            tradeType = "JSAPI";
        } else {
            tradeType = "APP";
        }
        return tradeType;
    }

}
