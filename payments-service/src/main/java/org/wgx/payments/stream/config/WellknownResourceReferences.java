package org.wgx.payments.stream.config;

/**
 * A utility class providing references to some well known resources.
 * @author weigu
 *
 */
public final class WellknownResourceReferences {

    private WellknownResourceReferences() { }

    /**
     * A reference to payment method list.
     */
    public static final String PAYMENT_METHOD_LIST = "Payment::Method::List";

    /**
     * A reference to internal payment method.
     */
    public static final String INTERNAL_PAYMENT_METHOD = "Internal::Payment::Method";

    /**
     * A reference to the new initiated payment request.
     */
    public static final String PAYMENT_REQUEST = "Payment::Request";
}
