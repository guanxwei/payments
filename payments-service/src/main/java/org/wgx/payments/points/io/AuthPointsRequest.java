package org.wgx.payments.points.io;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * A request to consume some points from the user's points account.
 * @author weigu
 *
 */
@Data
public class AuthPointsRequest implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6518813308982505516L;

    // Consume request id.
    private String requestID;

    // Reason to consume the points.
    private String reason;

    // Amount to be consumed.
    private String amount;

    // User id.
    private long userID;

    // References.
    private List<String> references;

    /** 
     * Flag indicates if it is okay to consume points less then the request amount when the user's points
     * account is not enough for the request.
     * 
     */
    private boolean partialTolarent;
}
