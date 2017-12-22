package org.wgx.payments.client.api.io;

import java.io.Serializable;
import java.util.List;
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
     * Payment method code list. At most two payment method codes can be presented.
     * In generic cases, clients will provide only one payment method code.
     * To support cases like paying the order via Alipay & GiftCard, the clients should provide both these payment method code.
     * Now multi-tender cases are not supported.
     */
    private List<String> paymentMethodName;

}
