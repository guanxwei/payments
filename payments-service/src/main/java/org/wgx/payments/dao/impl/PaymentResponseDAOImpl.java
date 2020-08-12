package org.wgx.payments.dao.impl;

import java.util.List;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.model.PaymentResponse;

/**
 * Mybatis based implementation of {@linkplain PaymentResponseDAO}.
 *
 */
public class PaymentResponseDAOImpl extends BaseFrameWorkDao<PaymentResponseDAO> implements PaymentResponseDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final PaymentResponse paymentResponse) {
        return getMapper().save(paymentResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(final PaymentResponse paymentResponse) {
        return getMapper().update(paymentResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentResponse getPaymentResponseByTransactionID(final String transactionID) {
        return getMapper().getPaymentResponseByTransactionID(transactionID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentResponse> getPaymentResponseListByCustomerIDAndOperationType(final long customerID,
            final String operationType) {
        return getMapper().getPaymentResponseListByCustomerIDAndOperationType(customerID, operationType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentResponse> getPaymentResponseListByRange(final String beginTime, final String endTime) {
        return getMapper().getPaymentResponseListByRange(beginTime, endTime);
    }

}
