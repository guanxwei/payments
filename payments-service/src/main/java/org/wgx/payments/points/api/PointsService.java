package org.wgx.payments.points.api;

import org.wgx.payments.points.io.ConsumePointsRequest;
import org.wgx.payments.points.io.ConsumePointsResponse;

/**
 * Points service.
 * @author weiguanxiong.
 *
 */
public interface PointsService {

    /**
     * Send request to points service to consume some points.
     * @param consumePointsRequest Request detail.
     * @return A response to the request.
     */
    ConsumePointsResponse consume(final ConsumePointsRequest consumePointsRequest);
}
