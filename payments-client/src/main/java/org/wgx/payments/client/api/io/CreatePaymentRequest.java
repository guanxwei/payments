package org.wgx.payments.client.api.io;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of requests to call payments platform to create corresponding payment requests.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CreatePaymentRequest extends Request implements Serializable {

    /**
     * Auto generated version id.
     */
    private static final long serialVersionUID = 3190958069884979650L;

    /**
     * Tag preserved for special cases.
     */
    private Map<String, String> specialTags;

    /**
     * Map of reference ids and their corresponding requested amount.
     */
    private Map<String, String> references;

    /**
     * Payment method code.
     */
    private Integer paymentMethod;

}
