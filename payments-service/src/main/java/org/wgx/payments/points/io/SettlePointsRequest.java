package org.wgx.payments.points.io;

import java.io.Serializable;

import lombok.Data;

/**
 * Request to settle points for the specific order.
 * @author weigu
 *
 */
@Data
public class SettlePointsRequest implements Serializable {

    private static final long serialVersionUID = 8367428201359970728L;

    // Target reference
    private String reference;

}
