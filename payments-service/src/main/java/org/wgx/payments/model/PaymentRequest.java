package org.wgx.payments.model;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Data;

/**
 * Internal payment request.
 */
@Data
public class PaymentRequest implements Serializable {

    private static final long serialVersionUID = 5286395862930889905L;

    // CHECKSTYLE:OFF
    private long id;
    private String transactionID;
    private String referenceID;
    private int status;
    private String channel;
    private Timestamp createTime;
    private Timestamp lastUpdateTime;
    private String requestedAmount;
    private String paymentMethod;
    private String parentID;
    private String customerID;
    private String url;
    private String paymentOperationType;
    private String callBackMetaInfo;
    private String business;
    // CHECKSTYLE:ON
}
