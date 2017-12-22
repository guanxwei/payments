package org.wgx.payments.dao;

import java.util.List;

import org.wgx.payments.model.PaymentRequest;

/**
 * PaymentRequestDAO.
 */
public interface PaymentRequestDAO {

    /**
     * Get PaymentRequest by reference id.
     * @param referenceID ID.
     * @param business Business name.
     * @return PaymentRequest list.
     */
    List<PaymentRequest> getPaymentRequestByReferenceIDAndBusinessName(final String referenceID, final String business);

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
     * @param transactionID Payment request's transaction ID.
     * @return PaymentRequest DB record.
     */
    PaymentRequest getPaymentRequestByTransactionID(final String transactionID);

    /**
     * Get PaymentRequest list by parent id.
     * @param parentID The parent request's transaction id.
     * @return PaymentRequest list.
     */
    List<PaymentRequest> getPaymentRequestsByParentID(final String parentID);

    /**
     * Get pending PaymentRequest list by payment operation type and payment method.
     * @param paymentOperationType Operation type.
     * @param paymentMethod Payment method.
     * @param status Payment request's status.
     * @param limit limit.
     * @return PaymentRequest list.
     */
    List<PaymentRequest> getPendingPaymentRequestList(final int status, final String paymentMethod,
            final String paymentOperationType, final int limit);
}
