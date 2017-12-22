package org.wgx.payments.utils;

import org.wgx.payments.signature.Account;

/**
 * Utility class to store payment account information in thread local.
 * @author hzweiguanxiong
 *
 */
public final class AccountUtils {

    private AccountUtils() { }

    private static final ThreadLocal<Account> ACCOUNT = new ThreadLocal<>();

    /**
     * Set account in thread local.
     * @param account Account to be used.
     */
    public static void set(final Account account) {
        ACCOUNT.set(account);
    }

    /**
     * Get account from the thread local.
     * @return Account.
     */
    public static Account get() {
        return ACCOUNT.get();
    }
}
