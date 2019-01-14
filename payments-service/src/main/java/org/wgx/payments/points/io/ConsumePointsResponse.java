package org.wgx.payments.points.io;

import java.io.Serializable;

import lombok.Data;

@Data
public class ConsumePointsResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6019423005599196660L;

    /**
     * Acknowledged amount.
     */
    private String acknowledgedAmount;

    // Processing response code.
    private int code;

    // Error message.
    private String message;

    // Transaction id used to refund the acknowledged amount.
    private String transactionID;

}
