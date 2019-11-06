package org.wgx.payments.dao;

import java.util.List;

import org.wgx.payments.account.impl.meta.PaymentAccount;

/**
 * Payment account DAO.
 *
 */
public interface PaymentAccountDAO {

    /**
     * Save new payment account.
     * @param account Payment account to be saved.
     * @return Manipulation result.
     */
    int save(final PaymentAccount account);

    /**
     * Update payment account basic information.
     * @param account Account to be updated.
     * @return Manipulation result.
     */
    int edit(final PaymentAccount account);

    /**
     * Find payment account by name.
     * @param accountName Account name.
     * @return Check result.
     */
    int getAccountNameCount(final String accountName);

    /**
     * Query payment account by key word.
     * @param key Key word.
     * @return Payment account list that fulfill requirement.
     */
    List<PaymentAccount> query(final String key);

    /**
     * Get payment account list by payment method name.
     * @param paymentMethod Payment method name.
     * @return Payment account list associated to the specific paymentMethod.
     */
    List<PaymentAccount> getByPaymentMethod(final int paymentMethod);

    /**
     * Get payment account by id.
     * @param id Account's id
     * @return Payment account if exists.
     */
    PaymentAccount get(final long id);
}
