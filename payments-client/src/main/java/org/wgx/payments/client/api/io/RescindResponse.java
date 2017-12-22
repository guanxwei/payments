package org.wgx.payments.client.api.io;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of rescind agreement response.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RescindResponse extends Response implements Serializable {

    /**
     * Auto generated version ID.
     */
    private static final long serialVersionUID = 1566691402574409602L;

}
