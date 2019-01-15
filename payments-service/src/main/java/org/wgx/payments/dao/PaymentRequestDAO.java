package org.wgx.payments.dao;

import org.wgx.payments.model.PaymentRequest;

/**
 * PaymentRequestDAO.
 */
public interface PaymentRequestDAO {

    /**
     * Save the PaymentRequest entity.
     * @param paymentRequest PaymentRequest.
     * @return DB manipulation result.
     */
    int save(final PaymentRequest paymentRequest);

    /**
     * Update the PaymentRequest entity.
     * @param paymentRequest PaymentRequest.
     * @return DB manipulation result.
     */
    int update(final PaymentRequest paymentRequest);

    /**
     * Get PaymentRequest by transaction id.
     * @param id Payment request's ID.
     * @return PaymentRequest DB record.
     */
    PaymentRequest getPaymentRequestID(final long id);

}
