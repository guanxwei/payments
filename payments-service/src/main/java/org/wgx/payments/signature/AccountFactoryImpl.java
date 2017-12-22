package org.wgx.payments.signature;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.env.Environment;
import org.wgx.payments.exception.AccountNotFoundException;
import org.wgx.payments.material.HeimdallrClient;
import org.wgx.payments.material.helper.MaterialRetrieveHelper;
import org.wgx.payments.material.io.RetrieveMaterialResponse;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@linkplain AccountFactory}.
 * @author hzweiguanxiong
 *
 */
@Slf4j
public class AccountFactoryImpl implements AccountFactory {

    private static final Map<String, Account> ACCOUNTS = new ConcurrentHashMap<>();
    private static final Map<String, String> ACCOUNT_TO_MATERIAL_MAPPING = new ConcurrentHashMap<>();
    private static final Map<String, String> MATERIAL_TO_ACCOUNT_MAPPING = new ConcurrentHashMap<>();
    private static final Map<String, String> ACCOUNT_NICKNAME_MAPPING = new ConcurrentHashMap<>();

    @Getter
    private List<String> materialNameList;
    @Getter
    private List<String> alipayAccountList;
    @Getter
    private List<String> wechatAccountList;
    @Getter
    private List<String> accountNameList;

    @Setter
    private String accountNames;
    @Setter
    private String nickNames;
    @Setter
    private String alipayAccounts;
    @Setter
    private String wechatAccounts;
    @Setter
    private String materialNames;
    @Setter
    private HeimdallrClient heimdallrClient;
    @Setter
    private Environment environment;

    /**
     * Pre-retrieve the materials to quicken application running speed.
     */
    public void init() {
        loadAccounts(accountNames);
        initiateMapping();
        loadSpecificAccounts();
        loadMaterials();
    }

    private void loadAccounts(final String names) {
        log.info("");
        accountNameList = new LinkedList<>();
        accountNameList.addAll(Arrays.asList(names.split(",")));
    }

    private void initiateMapping() {
        for (String account : accountNameList) {
            String materialName = environment.getProperty(account);
            ACCOUNT_TO_MATERIAL_MAPPING.put(account, materialName);
            MATERIAL_TO_ACCOUNT_MAPPING.put(materialName, account);
        }
    }

    private void loadSpecificAccounts() {
        String[] accounts = alipayAccounts.split(",");
        alipayAccountList = new LinkedList<>();
        alipayAccountList.addAll(Arrays.asList(accounts));
        accounts = wechatAccounts.split(",");
        wechatAccountList = new LinkedList<>();
        wechatAccountList.addAll(Arrays.asList(accounts));
    }

    private void loadMaterials() {
        String[] materialNameArray = materialNames.split(",");
        materialNameList = Arrays.asList(materialNameArray);
        for (String materialName : materialNameList) {
            try {
                RetrieveMaterialResponse response = heimdallrClient.retrieve(materialName);
                String publicKey = MaterialRetrieveHelper.getPublicKeyAsString(response.getMaterial());
                String privateKey = MaterialRetrieveHelper.getPrivateKeyAsString(response.getMaterial());
                String additional = MaterialRetrieveHelper.getAdditional(response.getMaterial());
                Account account = new Account();
                account.setMaterialName(materialName);
                account.setPrivateKey(privateKey);
                account.setPublicKey(publicKey);
                account.setAccountName(MATERIAL_TO_ACCOUNT_MAPPING.get(materialName));
                account.setAdditional(additional);
                ACCOUNTS.put(account.getAccountName(), account);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Get material name by account name.
     * @param accountName Account name.
     * @return Material name.
     */
    public String getMaterialNameByAccountName(final String accountName) {
        return ACCOUNT_TO_MATERIAL_MAPPING.get(accountName);
    }

    /**
     * Get account public key via name.
     * @param materialName Account's name.
     * @return Account's public key.
     */
    public String getPublicKeyByMaterialName(final String materialName) {
        String accountName = MATERIAL_TO_ACCOUNT_MAPPING.get(materialName);
        Account account = ACCOUNTS.get(accountName);
        if (account == null) {
            throw new AccountNotFoundException(String.format("Can not found account [%s]", materialName));
        }
        return account.getPublicKey();
    }

    /**
     * Get account private key via name.
     * @param materialName Account's name.
     * @return Account's private key.
     */
    public String getPrivateKeyByMaterialName(final String materialName) {
        String accountName = MATERIAL_TO_ACCOUNT_MAPPING.get(materialName);
        Account account = ACCOUNTS.get(accountName);
        if (account == null) {
            throw new AccountNotFoundException(String.format("Can not found account [%s]", materialName));
        }
        return account.getPrivateKey();
    }

    /**
     * Get account number via name.
     * @param accountName Account's name.
     * @return Account's number.
     */
    public String getAccountNoByAccountName(final String accountName) {
        Account account = ACCOUNTS.get(accountName);
        if (account == null) {
            throw new AccountNotFoundException(String.format("Can not found account [%s]", accountName));
        }
        return account.getAccountNo();
    }

    /**
     * Get payment account nick name.
     * @param accountName Account name.
     * @return Account nick name.
     */
    public String getNickname(final String accountName) {
        return ACCOUNT_NICKNAME_MAPPING.get(accountName);
    }

    /**
     * Get account.
     * @param accoutName Account name.
     * @return Account.
     */
    public Account getAccount(final String accoutName) {
        return ACCOUNTS.get(accoutName);
    }
}
