package org.wgx.payments.virtual.account.impl.meta;

import java.io.Serializable;

import lombok.Data;

/**
 * Payment account.
 *
 */
@Data
public class PaymentAccountScope implements Serializable {

    private static final long serialVersionUID = 4932382119219865106L;

    private long id;

    private long accountID;

    // supported payment operation.
    private String supportedOperations;

    // issuing banks
    private String issuingBanks;

    // issuer
    private String issuer;

    // device type
    private String deviceType;

    // supported business.
    private String supportedBusinesses;
}
