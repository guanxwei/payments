package org.wgx.payments.virtual.account.io;

import java.io.Serializable;

import lombok.Data;

/**
 * Encapsulation of Payment account response.
 * @author hzweiguanxiong
 *
 */
@Data
public class PaymentAccountResponse implements Serializable {

    private static final long serialVersionUID = 3992613030639383976L;

    // 状�?�码�? 200 成功�?404 没有匹配�?500 内部错误
    private int code;

    // 错误信息
    private String message;

    // 符合要求的账户名�?
    private String accountName;

    // 符合要求的账户号
    private String accountNo;
}
