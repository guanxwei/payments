package org.wgx.payments.client.api.io;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of refund request.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RefundRequest extends Request implements Serializable {

    /**
     * Auto generated version id.
     */
    private static final long serialVersionUID = -3558940687030019202L;

    /**
     * Charge request transaction ID.
     */
    private String transactionID;

    /**
     * Refund amount.
     */
    private String refundAmount;

    /**
     * Reference ID.
     */
    private String referenceID;

    /**
     * Tag preserved for special cases.
     */
    private Map<String, String> specialTags;

    /**
     * Unique identity in client system for single one refund request.
     */
    private String externalRefundID;
}
