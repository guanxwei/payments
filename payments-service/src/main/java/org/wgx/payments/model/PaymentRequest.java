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
    private String referenceID;
    private int status;
    private String channel;
    private Timestamp createTime;
    private Timestamp lastUpdateTime;
    private String requestedAmount;
    private int paymentMethod;
    private long customerID;
    private String paymentOperationType;
    private String callBackMetaInfo;
    private String business;
    /**
     * Multi-order pay senarios needed, when the client initiate the multi-orders, all the orders should be payed
     * in one turn-around.
     */
    private long parentRequestID;
    private String transactionID;
    // CHECKSTYLE:ON
}
