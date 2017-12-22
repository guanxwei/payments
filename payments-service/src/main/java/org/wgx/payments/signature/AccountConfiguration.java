package org.wgx.payments.signature;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.wgx.payments.material.HeimdallrClient;

/**
 * Account configuration class.
 *
 */
@Configuration
public class AccountConfiguration {

    @Resource(name = "environment")
    private Environment environment;

    @Resource(name = "heimdallrClient")
    private HeimdallrClient heimdallrClient;

    /**
     * Account factory definition method.
     * @return AccountFactory.
     */
    @Bean(name = "accountFactory", initMethod = "init")
    public AccountFactory accountFactory() {
        AccountFactoryImpl accountFactory =  new AccountFactoryImpl();
        String materialNames = environment.getProperty("payment.material.name.list");
        accountFactory.setMaterialNames(materialNames);
        accountFactory.setEnvironment(environment);

        accountFactory.setAccountNames(environment.getProperty("payment.accounts"));
        accountFactory.setAlipayAccounts(environment.getProperty("alipay.accounts"));
        accountFactory.setWechatAccounts(environment.getProperty("wechat.accounts"));
        accountFactory.setHeimdallrClient(heimdallrClient);
        accountFactory.setNickNames(environment.getProperty("payment.account.nickname"));
        return accountFactory;
    }
}
