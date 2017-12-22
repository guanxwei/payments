package org.wgx.payments.dao;

import java.util.List;

import org.wgx.payments.model.PaymentResponse;

/**
 * PaymentRequestDAO.
 */
public interface PaymentResponseDAO {

    /**
     * Get PaymentResponse by reference id.
     * @param referenceID ID.
     * @param business Business name.
     * @return PaymentResponse list.
     */
    List<PaymentResponse> getPaymentResponseByReferenceIDAndBusiness(final String referenceID, final String business);

    /**
     * Save the PaymentRequest information.
     * @param paymentResponse PaymentResponse.
     * @return Result.
     */
    int save(final PaymentResponse paymentResponse);

    /**
     * Update the PaymentRequest information.
     * @param paymentResponse PaymentResponse.
     * @return Result.
     */
    int update(final PaymentResponse paymentResponse);

    /**
     * Get PaymentResponse by signature and processor name.
     * @param transactionID TransactionID.
     * @return Payment response.
     */
    PaymentResponse getPaymentResponseByTransactionID(final String transactionID);

    /**
     * Get payment response list by customer id and operation type. Mainly used in Sign cases.
     * @param customerID Customer identifier.
     * @param operationType Operation type.
     * @return Payment response list.
     */
    List<PaymentResponse> getPaymentResponseListByCustomerIDAndOperationType(final String customerID,
            final String operationType);

    /**
     * Get payment response list by time range and payment method.
     * @param beginTime Begin of the range.
     * @param endTime End of the range.
     * @return Payment response list.
     */
    List<PaymentResponse> getPaymentResponseListByRange(final String beginTime, final String endTime);
}
