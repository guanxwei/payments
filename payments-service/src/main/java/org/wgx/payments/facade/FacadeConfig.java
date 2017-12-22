package org.wgx.payments.facade;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Facade configuration class.
 *
 */
@Configuration
public class FacadeConfig {

    /**
     * IAP facade bean definition.
     * @return IAPFacade.
     */
    @Bean(name = "IAPFacade")
    public Facade<Pair<String, String>, Boolean> iapFacade() {
        return new IAPFacade();
    }

    /**
     * IAP facade bean definition.
     * @return IAPFacade.
     */
    @Bean(name = "alipayFacade")
    public Facade<Pair<String, String>, String> alipayFacade() {
        return new AlipayFacade();
    }

    /**
     * Wechat facade bean definition.
     * @return WechatFacade
     */
    @Bean(name = "wechatFacade")
    public Facade<Pair<String, String>, String> wechatFacade() {
        return new WechatFacade();
    }
}
