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

    // ä»¥é?—å·åˆ†éš”å¼?çš„payment operation ä»£ç ï¼? å¦? charge,refund
    private String supportedOperations;

    // ä»¥é?—å·åˆ†éš”å¼?çš„å¼€å¡è¡Œä»£ç å¦‚CMB,MBCCç­‰ã??
    private String issuingBanks;

    // å¼?å¡è¡Œ.
    private String issuer;

    // è®¾å¤‡å?
    private String deviceType;

    // ä»¥é?—å·åˆ†éš”å¼?çš„business profiles ä»£ç ï¼? å¦? Retail,VIP
    private String supportedBusinesses;
}
