package org.wgx.payments.account.impl.meta;

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

    // 以逗号分隔开的payment operation 代码， 如 charge,refund
    private String supportedOperations;

    // 以逗号分隔开的开卡行代码如CMB,MBCC等。
    private String issuingBanks;

    // 开卡行.
    private String issuer;

    // 设备号
    private String deviceType;

    // 以逗号分隔开的business profiles 代码， 如 Retail,VIP
    private String supportedBusinesses;
}
