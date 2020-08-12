package org.wgx.payments.virtual.account.dao.impl;

import java.util.List;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.DAOMethod;
import org.wgx.payments.virtual.account.dao.PaymentAccountDAO;
import org.wgx.payments.virtual.account.impl.meta.PaymentAccount;

/**
 * Mybatis based implementation of {@linkplain PaymentAccountDAO}.
 *
 */
public class PaymentAccountDAOImpl extends BaseFrameWorkDao<PaymentAccountDAO>
        implements PaymentAccountDAO {

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public int save(final PaymentAccount account) {
        return getMapper().save(account);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public int edit(final PaymentAccount account) {
        return getMapper().edit(account);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public int getAccountNameCount(final String accountName) {
        return getMapper().getAccountNameCount(accountName);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public List<PaymentAccount> query(final String key) {
        return getMapper().query(key);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public List<PaymentAccount> getByPaymentMethod(final int paymentMethod) {
        return getMapper().getByPaymentMethod(paymentMethod);
    }

    /**
     * {@inheritDoc}
     */
    @DAOMethod
    @Override
    public PaymentAccount get(final long id) {
        return getMapper().get(id);
    }

}
