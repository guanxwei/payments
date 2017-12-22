package org.wgx.payments.facade;

/**
 * Abstract of facade system. Used to call external system services.
 *
 * @param <T>
 * @param <U>
 */
public interface Facade<T, U> {

    /**
     * Call external service.
     * @param t Request information.
     * @return Reponse from external services.
     */
    U call(final T t);

}
