package org.wgx.payments.deducer;

/**
 * Basic deducer interface. Each deducer will deduce something useful from the input.
 *
 * @param <T> input
 * @param <U> output
 */
@FunctionalInterface
public interface Deducer<T, U> {

    /**
     * Deduce output from the input.
     * @param t input
     * @return Result.
     */
    U deduce(T t);

}
