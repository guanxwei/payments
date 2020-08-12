package org.wgx.payments.virtual.account.dao.impl;

import org.wgx.payments.dao.BaseFrameWorkDao;
import org.wgx.payments.virtual.account.dao.PaymentAccountScopeDAO;
import org.wgx.payments.virtual.account.impl.meta.PaymentAccountScope;

/**
 * Mybatis based implementation of {@linkplain PaymentAccountScopeDAO}.
 *
 */
public class PaymentAccountScopeDAOImpl extends BaseFrameWorkDao<PaymentAccountScopeDAO> implements PaymentAccountScopeDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public int save(final PaymentAccountScope scope) {
        return getMapper().save(scope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int edit(final PaymentAccountScope scope) {
        return getMapper().edit(scope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAccountCount(final long accountID) {
        return getMapper().getAccountCount(accountID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentAccountScope find(final long accountID) {
        return getMapper().find(accountID);
    }

}
