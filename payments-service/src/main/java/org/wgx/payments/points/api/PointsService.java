package org.wgx.payments.points.api;

import org.wgx.payments.points.io.AuthPointsRequest;
import org.wgx.payments.points.io.AuthPointsResponse;

/**
 * Points service.
 * @author weiguanxiong.
 *
 */
public interface PointsService {

    /**
     * Send request to points service to authorize some points.
     * @param consumePointsRequest Request detail.
     * @return A response to the request.
     */
    AuthPointsResponse auth(final AuthPointsRequest authPointsRequests);

}
