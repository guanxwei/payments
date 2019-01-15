package org.wgx.payments.dao.impl;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.PaymentRequestDAO;
import org.wgx.payments.model.PaymentRequest;

/**
 * Mybatis based implementation of {@linkplain PaymentRequestDAO}.
 *
 */
public class PaymentRequestDAOImpl extends BaseFrameWorkDao implements PaymentRequestDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final PaymentRequest paymentRequest) {
        return process(() -> getMapper(PaymentRequestDAO.class).save(paymentRequest));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(final PaymentRequest paymentRequest) {
        return process(() -> getMapper(PaymentRequestDAO.class).update(paymentRequest));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentRequest getPaymentRequestID(final long id) {
        return process(() -> getMapper(PaymentRequestDAO.class).getPaymentRequestID(id));
    }

}
