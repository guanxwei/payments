package org.wgx.payments.virtual.account;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wgx.payments.virtual.account.impl.service.PaymentAccountServiceImpl;
@Configuration
public class PaymentAccountConfiguration {

    @Bean
    public PaymentAccountService paymentAccountService() {
        return new PaymentAccountServiceImpl();
    }

}
