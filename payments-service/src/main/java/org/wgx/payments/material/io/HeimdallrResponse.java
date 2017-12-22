package org.wgx.payments.material.io;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for all the Heimdallr services' responses.
 * @author hzweiguanxiong
 *
 */
@Getter
@Setter
public class HeimdallrResponse {

    /**
     * Response status code.
     */
    private int code;

    /**
     * Error message.
     */
    private String message;
}
