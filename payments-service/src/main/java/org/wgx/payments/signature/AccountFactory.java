package org.wgx.payments.signature;

import java.util.List;

/**
 * Account factory oriented to Payments internal components.
 * All the account will actually be retrieved from Heimdallr service. Developers can treat this
 * factory as the memory cache for the accounts.
 *
 */
public interface AccountFactory {

    /**
     * Pre-retrieve the materials to quicken application running speed.
     */
    void init();

    /**
     * Get material name by account name.
     * @param accountName Account name.
     * @return Material name.
     */
    String getMaterialNameByAccountName(final String accountName);

    /**
     * Get account public key via name.
     * @param materialName Account's name.
     * @return Account's public key.
     */
    String getPublicKeyByMaterialName(final String materialName);

    /**
     * Get account private key via name.
     * @param materialName Account's name.
     * @return Account's private key.
     */
    String getPrivateKeyByMaterialName(final String materialName);

    /**
     * Get account number via name.
     * @param accountName Account's name.
     * @return Account's number.
     */
    String getAccountNoByAccountName(final String accountName);

    /**
     * Get payment account nick name.
     * @param accountName Account name.
     * @return Account nick name.
     */
    String getNickname(final String accountName);

    /**
     * Get account.
     * @param accoutName Account name.
     * @return Account.
     */
    Account getAccount(final String accoutName);

    /**
     * Get Wechat related payment account name list.
     * @return Wechat account name list.
     */
    List<String> getWechatAccountList();

    /**
     * Get Alipay related payment account name list.
     * @return Alipay account name list.
     */
    List<String> getAlipayAccountList();
}
