package org.wgx.payments.jumpbox.io;

import java.io.Serializable;

import lombok.Data;

/**
 * Request from payment service to jump box platform.
 * Jump box platform will translate the internal requests into
 * external world specific requests.
 *
 * From the system managers' perspective, the procedure will be internal systems --(payments client api)--> payments service
 * --(jump box api)--> jump box --(http api)--> external world.
 * @author weiguanxiong
 *
 */
@Data
public class PaymentsRequest implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5432460607013409712L;

    /**
     * Payment method code.
     */
    private int paymentMethod;

    /**
     * Payment operation type.
     */
    private String operationType;

    /**
     * Payment account.
     */
    private String account;

    /**
     * Signature key.
     */
    private String key;
}
