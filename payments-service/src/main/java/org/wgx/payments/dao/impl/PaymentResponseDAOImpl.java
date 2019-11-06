package org.wgx.payments.dao.impl;

import java.util.List;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.PaymentResponseDAO;
import org.wgx.payments.model.PaymentResponse;

/**
 * Mybatis based implementation of {@linkplain PaymentResponseDAO}.
 *
 */
public class PaymentResponseDAOImpl extends BaseFrameWorkDao implements PaymentResponseDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final PaymentResponse paymentResponse) {
        return process(() -> getMapper(PaymentResponseDAO.class).save(paymentResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(final PaymentResponse paymentResponse) {
        return process(() -> getMapper(PaymentResponseDAO.class).update(paymentResponse));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentResponse getPaymentResponseByTransactionID(final String transactionID) {
        return process(() -> getMapper(PaymentResponseDAO.class).getPaymentResponseByTransactionID(transactionID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentResponse> getPaymentResponseListByCustomerIDAndOperationType(final long customerID,
            final String operationType) {
        return process(() -> getMapper(PaymentResponseDAO.class).getPaymentResponseListByCustomerIDAndOperationType(customerID, operationType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentResponse> getPaymentResponseListByRange(final String beginTime, final String endTime) {
        return process(() -> getMapper(PaymentResponseDAO.class).getPaymentResponseListByRange(beginTime, endTime));
    }

}
