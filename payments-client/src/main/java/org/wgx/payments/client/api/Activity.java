package org.wgx.payments.client.api;

/**
 * Base interface of payments services.
 *
 * @param <T> input request.
 * @param <U> output response.
 */
@FunctionalInterface
public interface Activity<T, U> {

    /**
     * Entrance of payments services.
     * @param request Input object.
     * @return Processing result.
     */
    U execute(final T request);

}
