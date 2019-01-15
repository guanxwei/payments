package org.wgx.payments.dao;

import java.util.List;

import org.wgx.payments.model.PaymentExecutionRecord;

/**
 * Data access layer object for entity {@link PaymentExecutionRecord}.
 * @author weiguanxiong
 *
 */
public interface PaymentExecutionRecordDAO {

    /**
     * Save a new payment execution record in db.
     * @param paymentExecutionRecord Payment execution record to be saved.
     * @return {@code 1} succeed, {@code 0} failed.
     */
    int save(final PaymentExecutionRecord paymentExecutionRecord);

    /**
     * Find a record by id.
     * @param id Record id.
     * @return PaymentExecutionRecord entity.
     */
    PaymentExecutionRecord find(final long id);

    /**
     * Find payment execution records for the specific request.
     * @param paymentRequestID Payment request id.
     * @return PaymentExecutionRecord entity list.
     */
    List<PaymentExecutionRecord> findByPaymentRequestID(final long paymentRequestID);
}
