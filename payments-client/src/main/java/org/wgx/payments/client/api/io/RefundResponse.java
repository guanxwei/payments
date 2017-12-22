package org.wgx.payments.client.api.io;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of refund response.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RefundResponse extends Response implements Serializable {

    /**
     * Auto generated version ID.
     */
    private static final long serialVersionUID = 7946998008582856068L;

}
