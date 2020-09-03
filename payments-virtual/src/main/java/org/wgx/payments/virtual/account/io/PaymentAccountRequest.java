package org.wgx.payments.virtual.account.io;

import java.io.Serializable;

import lombok.Data;

/**
 * Encapsulation of payment account request.
 *
 */
@Data
public class PaymentAccountRequest implements Serializable {

    private static final long serialVersionUID = 428403949413094799L;

    // related business
    private String business;

    // payment method code
    private int paymentMethod;

    // device type if specified
    private String deviceType;

    // customer region.
    private String region;

    // Credit or Debit card used only indicating which bank the card is issued.
    private String issueingBank;

    // Card issuing institution.
    private String issuer;

    // Payment operation charge auth or settle.
    private String paymentOperation;
}
