package org.wgx.payments.client.api.io;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of query payment transaction status response.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class QueryPaymentTransactionResponse extends Response implements Serializable {

    /**
     * Auto generated serial version id.
     */
    private static final long serialVersionUID = -6316386343570538847L;

    /**
     * The payment transaction status.
     */
    private int transactionStatus;

}
