package org.wgx.payments.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Bill checking item table.
 *
 */
@Data
@Builder
@AllArgsConstructor
public class CheckbookItem {

    public CheckbookItem() { }

    private long id;

    private String transactionID;

    private int status;

    private String business;

    private String referenceID;

    private String type;

    private String externalTransactionID;

    private String requestedAmount;

    private String acknowledgedAmount;

    private Timestamp recordTime;

    private Timestamp transactionTime;

    private String paymentMethod;

    private String uniqueKey;
}
