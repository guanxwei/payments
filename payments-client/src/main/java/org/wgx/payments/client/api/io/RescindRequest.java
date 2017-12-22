package org.wgx.payments.client.api.io;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of rescind agreement request.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RescindRequest extends Request implements Serializable {

    /**
     * Auto generated version ID.
     */
    private static final long serialVersionUID = 7750081808636735193L;

    /**
     * The payment method name.
     */
    private String paymentMethodName;

    /**
     * Tag preserved for special cases.
     */
    private Map<String, String> specialTags;
}
