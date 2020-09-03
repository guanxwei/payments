package org.wgx.payments.jumpbox.communicator;

import java.util.Map;

/**
 * Abstract of communicator between payments platform and external systems.
 * @author weiguanxiong
 *
 */
public interface Communicator {

    /**
     * Send requests to our co-operate external systems.
     * @param method Http method type.
     * @param type Content type.
     * @param parameters Paramters to be sent.
     * @return Response body.
     */
    byte[] send(final String method, final String type, final Map<String, Object> parameters);
}
