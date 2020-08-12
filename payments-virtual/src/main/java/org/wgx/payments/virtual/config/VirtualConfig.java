package org.wgx.payments.virtual.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.wgx.payments.virtual.account.dao.PaymentAccountDAO;
import org.wgx.payments.virtual.account.dao.PaymentAccountScopeDAO;
import org.wgx.payments.virtual.account.dao.impl.PaymentAccountDAOImpl;
import org.wgx.payments.virtual.account.dao.impl.PaymentAccountScopeDAOImpl;

@ImportResource(locations = {"classpath:spring-mybatis.xml"})
@PropertySource({"classpath:jdbc.properties"})
@Configuration
public class VirtualConfig {

    @Bean
    public PaymentAccountDAO paymentAccountDAO() {
        PaymentAccountDAOImpl impl = new PaymentAccountDAOImpl();
        return impl;
    }

    @Bean
    public PaymentAccountScopeDAO paymentAccountScopeDAO() {
        return new PaymentAccountScopeDAOImpl();
    }
}
