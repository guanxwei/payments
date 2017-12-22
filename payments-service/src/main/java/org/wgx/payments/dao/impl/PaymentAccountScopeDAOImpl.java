package org.wgx.payments.dao.impl;

import org.wgx.payments.account.impl.meta.PaymentAccountScope;
import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.dao.PaymentAccountScopeDAO;

/**
 * Mybatis based implementation of {@linkplain PaymentAccountScopeDAO}.
 *
 */
public class PaymentAccountScopeDAOImpl extends BaseFrameWorkDao implements PaymentAccountScopeDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final PaymentAccountScope scope) {
        return process(() -> getMapper(PaymentAccountScopeDAO.class).save(scope));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int edit(final PaymentAccountScope scope) {
        return process(() -> getMapper(PaymentAccountScopeDAO.class).edit(scope));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAccountCount(final long accountID) {
        return process(() -> getMapper(PaymentAccountScopeDAO.class).getAccountCount(accountID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentAccountScope find(final long accountID) {
        return process(() -> getMapper(PaymentAccountScopeDAO.class).find(accountID));
    }

}
