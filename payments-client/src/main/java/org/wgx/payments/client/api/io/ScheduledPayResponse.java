package org.wgx.payments.client.api.io;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of scheduled payment response.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ScheduledPayResponse extends Response implements Serializable {

    /**
     * Auto generated version ID.
     */
    private static final long serialVersionUID = 8487055611114175523L;

}
