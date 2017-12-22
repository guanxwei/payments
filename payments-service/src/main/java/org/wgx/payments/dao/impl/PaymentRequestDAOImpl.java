package org.wgx.payments.dao.impl;

import java.util.List;

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
    public List<PaymentRequest> getPaymentRequestByReferenceIDAndBusinessName(final String referenceID, final String business) {
        return process(() -> getMapper(PaymentRequestDAO.class).getPaymentRequestByReferenceIDAndBusinessName(referenceID, business));
    }

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
    public PaymentRequest getPaymentRequestByTransactionID(final String transactionID) {
        return process(() -> getMapper(PaymentRequestDAO.class).getPaymentRequestByTransactionID(transactionID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentRequest> getPaymentRequestsByParentID(final String parentID) {
        return process(() -> getMapper(PaymentRequestDAO.class).getPaymentRequestsByParentID(parentID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentRequest> getPendingPaymentRequestList(final int status, final String paymentMethod,
            final String paymentOperationType, final int limit) {
        return process(() -> getMapper(PaymentRequestDAO.class).getPendingPaymentRequestList(status, paymentMethod, paymentOperationType, limit));
    }

}
