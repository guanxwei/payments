package org.wgx.payments.client.api.io;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of create payment request response.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CreatePaymentResponse extends Response implements Serializable {

    /**
     * Auto generated serial version id.
     */
    private static final long serialVersionUID = -3915412204679636936L;

    /**
     * Key-value pairs, clients can use them to construct request form to call 3P payment gateways, especially when we
     * need to send HTTP POST request to 3P payment gateways.
     */
    private Map<String, String> values;

    /**
     * Single string value returned for scenarios when redirect URL is needed.
     */
    private String url;

}
