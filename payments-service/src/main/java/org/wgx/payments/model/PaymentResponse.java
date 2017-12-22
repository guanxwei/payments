package org.wgx.payments.model;

import java.sql.Timestamp;

import lombok.Data;

/**
 * Internal payment response.
 */
@Data
public class PaymentResponse {

    // CHECKSTYLE:OFF
    private long id;
    private String externalTransactionID;
    private String transactionID;
    /**
     * Payment response, 1 indicates success, 0 indicates failure.
     */
    private int status;
    private String rawResponse;
    private Timestamp createTime;
    private Timestamp lastUpdateTime;
    private String referenceID;
    private String paymentMethod;
    private String operationType;
    private String acknowledgedAmount;
    private String business;
    private String customerID;
    // CHECKSTYLE:ON

}
