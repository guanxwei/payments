package org.wgx.payments.model;

import java.sql.Timestamp;

import lombok.Data;

/**
 * 对账差异明细.
 * author hzxuwei3.
 * date 2017年4月20日 下午7:27:15
 */
@Data
public class CheckOrderDiffItem {
    private long id;
    private String transactionID;
    private String jobId;
    private int status;
    private Timestamp createTime;
    private Timestamp lastUpdateTime;
    private String referenceID;
    private String paymentMethod;
    private String operationType;
    private String acknowledgedAmount;
    private String business;
    private String customerID;
}
