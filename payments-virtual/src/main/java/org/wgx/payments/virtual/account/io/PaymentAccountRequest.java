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

    // 业务代码
    private String business;

    // 支付方式
    private int paymentMethod;

    // 设备代码
    private String deviceType;

    // 支付区域�? CN,EU�?
    private String region;

    // 发卡�?, 信用卡预留， CMB�?
    private String issueingBank;

    // 发卡组织，信用卡预留�? MasterCard�?
    private String issuer;

    // 请求的支付类型，扣款，�??款等
    private String paymentOperation;
}
