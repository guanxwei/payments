package org.wgx.payments.dao.impl;

import java.util.List;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.PaymentStatisticDAO;
import org.wgx.payments.model.PaymentStatistic;

/**
 * Mybatis based implementation of {@link PaymentStatisticDAO}.
 *
 */
public class PaymentStatisticDAOImpl extends BaseFrameWorkDao implements PaymentStatisticDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final PaymentStatistic paymentStatistic) {
        return process(() -> getMapper(PaymentStatisticDAO.class).save(paymentStatistic));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentStatistic> getLatestByBusiness(final String business, final int date) {
        return process(() -> getMapper(PaymentStatisticDAO.class).getLatestByBusiness(business, date));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentStatistic> getLatestByBusinessAndPaymentMethod(final String business, final String paymentMethod, final int date) {
        return process(() -> getMapper(PaymentStatisticDAO.class).getLatestByBusinessAndPaymentMethod(business, paymentMethod, date));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentStatistic> getByDate(final int begin, final int end) {
        return process(() -> getMapper(PaymentStatisticDAO.class).getByDate(begin, end));
    }

}
