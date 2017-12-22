package org.wgx.payments.model;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Data;

/**
 * Encapsulation of Fast search table item.
 *
 * These item are mainly used to fast payment transaction id query. Since DDB uses transaction id as {@linkplain PaymentRequest}
 * and {@linkplain PaymentResponse} policy key, using transaction ID to query and find related payment request and payment response
 * instance is much faster then any other way. For example, when we want to query the related charge payment request via reference id
 * and business, we can use sql like "select * from payment_request where referenceID = *** and business = ***", then the DDB has to
 * send query request to all the nodes since the policy id is not know. But with fastsearch_table_item table, we can save two records
 * in DB including : fastsearch_table_item and payment_request, while the key of fastsearch_table_item is "charge" + refereneceID + business.
 * As result, we separate one query into two, the first it to get the payment request's transactionID, while the second is to query
 * the payment request via the queried transactionID in first sql statement.
 *
 */
@Data
public class FastSearchTableItem implements Serializable {

    private static final long serialVersionUID = -931613666885754325L;

    /**
     * Record id.
     */
    private long id;

    /**
     * Fast search itemKey.
     */
    private String itemKey;

    /**
     * Payment-platform's transaction id.
     */
    private String transactionID;

    /**
     * Record time.
     */
    private Timestamp time;

    /**
     * Item status.
     */
    private int status;

    /**
     * Optional message.
     */
    private String message;
}
