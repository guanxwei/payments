package org.wgx.payments.virtual.account.dao;

import org.wgx.payments.virtual.account.impl.meta.PaymentAccountScope;

/**
 * PaymentAccountScopeDAO.
 * @author hzweiguanxiong
 *
 */
public interface PaymentAccountScopeDAO {

    /**
     * Save new payment account scope.
     * @param scope Payment account scope to be saved.
     * @return Manipulation result.
     */
    int save(final PaymentAccountScope scope);

    /**
     * Update payment account scope basic information.
     * @param scope Account scope to be updated.
     * @return Manipulation result.
     */
    int edit(final PaymentAccountScope scope);

    /**
     * Check if the payment account has been configured scope.
     * @param accountID Payment account id.
     * @return Checking result.
     */
    int getAccountCount(final long accountID);

    /**
     * Try to find payment account scope by account id.
     * @param accountID Payment account id.
     * @return Payment account scope entity if exists.
     */
    PaymentAccountScope find(final long accountID);
}
