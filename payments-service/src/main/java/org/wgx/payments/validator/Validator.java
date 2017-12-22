package org.wgx.payments.validator;

/**
 * Abstract of validator.
 *
 * Used to validate if the input meets specific demand.
 * @param <T>
 */
public interface Validator<T> {

    /**
     * Validate the input.
     * @param input Anything needs to be validated.
     * @return Validation result.
     */
    boolean validate(final T input);

}
