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

    // 以�?�号分隔�?的payment operation 代码�? �? charge,refund
    private String supportedOperations;

    // 以�?�号分隔�?的开卡行代码如CMB,MBCC等�??
    private String issuingBanks;

    // �?卡行.
    private String issuer;

    // 设备�?
    private String deviceType;

    // 以�?�号分隔�?的business profiles 代码�? �? Retail,VIP
    private String supportedBusinesses;
}
