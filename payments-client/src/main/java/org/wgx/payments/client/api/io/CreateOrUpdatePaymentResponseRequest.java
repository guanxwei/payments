package org.wgx.payments.client.api.io;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Encapsulation of raw 3P payment gateway response.
 *
 * When payments platform's callback listener receives a payment response from the 3P payment gateway like Alipay,
 * it will first assemble the raw HTTP request into {@link CreateOrUpdatePaymentResponseRequest}
 * then deliver it to the right processor to handle the response.
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CreateOrUpdatePaymentResponseRequest extends Request implements Serializable {

    /**
     * Auto generated serial version id.
     */
    private static final long serialVersionUID = -7428996792849285089L;

    /**
     * Raw query string from 3P payment gateway.
     */
    private String queryString;

    /**
     * Raw parameters from 3P payment gateway.
     */
    private Map<String, String[]> parameters;

    /**
     * Payment method code.
     */
    private String paymentMethodName;

}
