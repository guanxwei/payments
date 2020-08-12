package org.wgx.payments.dao.impl;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.model.PaymentRequest;

/**
 * Mybatis based implementation of {@linkplain PaymentRequestDAO}.
 *
 */
public class PaymentRequestDAOImpl extends BaseFrameWorkDao<PaymentRequestDAO> implements PaymentRequestDAO {

    private static final String TABLE = "PaymentRequest";

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final PaymentRequest paymentRequest) {
        return getMapper().save(paymentRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(final PaymentRequest paymentRequest) {
        return getMapper().update(paymentRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentRequest getPaymentRequestByID(final long id) {
        return getMapper().getPaymentRequestByID(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentRequest getPaymentRequestByTransactionID(String transactionID) {
        return getMapper().getPaymentRequestByTransactionID(transactionID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long allocateID() {
        return super.allocatedID(TABLE);
    }

}
