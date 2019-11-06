package org.wgx.payments.dao.impl;

import java.util.List;

import org.wgx.payments.account.impl.meta.PaymentAccount;
import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.PaymentAccountDAO;

/**
 * Mybatis based implementation of {@linkplain PaymentAccountDAO}.
 *
 */
public class PaymentAccountDAOImpl extends BaseFrameWorkDao implements PaymentAccountDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final PaymentAccount account) {
        return process(() -> getMapper(PaymentAccountDAO.class).save(account));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int edit(final PaymentAccount account) {
        return process(() -> getMapper(PaymentAccountDAO.class).edit(account));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAccountNameCount(final String accountName) {
        return process(() -> getMapper(PaymentAccountDAO.class).getAccountNameCount(accountName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentAccount> query(final String key) {
        return process(() -> getMapper(PaymentAccountDAO.class).query(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentAccount> getByPaymentMethod(final int paymentMethod) {
        return process(() -> getMapper(PaymentAccountDAO.class).getByPaymentMethod(paymentMethod));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentAccount get(final long id) {
        return process(() -> getMapper(PaymentAccountDAO.class).get(id));
    }

}
