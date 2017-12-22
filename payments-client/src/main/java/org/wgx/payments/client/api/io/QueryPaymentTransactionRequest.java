package org.wgx.payments.client.api.io;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of query payment transaction status request.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class QueryPaymentTransactionRequest extends Request implements Serializable {

    /**
     * Auto generated serial version id.
     */
    private static final long serialVersionUID = 798444161789741800L;

    /**
     * Reference id.
     */
    private String referenceID;

    /**
     * Payments internal transaction ID.
     */
    private String transactionID;

    /**
     * Operation type needs to be queried.
     */
    private String operationType;

}
