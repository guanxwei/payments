package org.wgx.payments.client.api.io;

import lombok.Getter;
import lombok.Setter;

/**
 * Base payments response.
 *
 */
@Getter @Setter
public class Response {

    /**
     * Response status code.
     */
    private int responseCode;

    /**
     * Response code description.
     */
    private String responseDescription;

    /**
     * Payments internal transaction ID.
     */
    private String transactionID;
}
