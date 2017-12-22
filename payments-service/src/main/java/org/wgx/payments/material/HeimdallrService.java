package org.wgx.payments.material;

/**
 * Base interface 
 * @author hzweiguanxiong
 *
 * @param <T>
 * @param <U>
 */
public interface HeimdallrService<T, U> {

    /**
     * Entrance of Heimdallr services.
     * @param request Input object.
     * @return Processing result.
     */
    U execute(final T request);

}
