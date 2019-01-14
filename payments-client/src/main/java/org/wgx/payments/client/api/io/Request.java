package org.wgx.payments.client.api.io;

import lombok.Getter;
import lombok.Setter;

/**
 * Base payment request.
 */
@Getter @Setter
public class Request {

    /**
     * Payment operation type.
     */
    private String paymentOperationType;

    /**
     * Callback information.
     */
    private CallbackMetaInfo callbackInfo;

    /**
     * Business name.
     */
    private String business;

    /**
     * Customer unique identifier.
     */
    private long customerID;

    /**
     * Client channel representation, like WAP, APP, PC, etc.
     * Some payment processors need this value to differ request data.
     *
     * Also known as deviceType.
     */
    private String channel;

}
