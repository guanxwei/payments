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
    private long requestID;
    /**
     * Payment response, 1 indicates success, 0 indicates failure.
     */
    private int status;
    private String rawResponse;
    private Timestamp createTime;
    private String acknowledgedAmount;
    private long customerID;
    private String referenceID;
    // CHECKSTYLE:ON

}
