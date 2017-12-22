package org.wgx.payments.client.api.io;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

/**
 * Encapsulation of callback meta information.
 *
 */
@Data
public class CallbackMetaInfo implements Serializable {

    /**
     * Auto generated version id.
     */
    private static final long serialVersionUID = 5506949722490258394L;

    /**
     * The HTTP api that will be called after the payment transaction is completed.
     */
    private String callBackUrl;

    /**
     * Extra parameters that should be pushed back to the clients.
     */
    private Map<String, String> parameters;
}
