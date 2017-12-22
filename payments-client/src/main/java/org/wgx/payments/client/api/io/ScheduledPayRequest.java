package org.wgx.payments.client.api.io;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of scheduled payment request.
 * The VIP system will periodic automatically charge money for Premium customers.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ScheduledPayRequest extends Request implements Serializable {

    /**
     * Auto generated version ID.
     */
    private static final long serialVersionUID = -3209207094668131157L;

    /**
     * Requested amount.
     */
    private String amount;
}
