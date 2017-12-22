package org.wgx.payments.account.impl.meta;

import java.io.Serializable;

import lombok.Data;

/**
 * Payment account.
 *
 */
@Data
public class PaymentAccount implements Serializable {

    private static final long serialVersionUID = -2677593872340073334L;

    private long id;

    // This name should be the same with the corresponding material name configured in Heimdallr service.
    private String accountName;

    // The account number assigned by 3P payment gateway, like merchant_id by Wechat and partnerID by Alipay.
    private String accountNo;

    private String paymentMethod;
}
