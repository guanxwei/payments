package org.wgx.payments.account;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wgx.payments.account.impl.service.PaymentAccountServiceImpl;

@Configuration()
public class PaymentAccountConfiguration {

    @Bean
    public PaymentAccountService paymentAccountService() {
        return new PaymentAccountServiceImpl();
    }

    @Bean
    public PaymentAccountClient paymentAccountClient() {
        return new PaymentAccountClient();
    }
}
