package org.wgx.payments.validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wgx.payments.client.api.io.CreateOrUpdatePaymentResponseRequest;

/**
 * Validator configuration class.
 *
 */
@Configuration
public class ValidatorConfig {

    /**
     * IAP validator.
     * @return IAPValidator.
     */
    @Bean(name = "IAPValidator")
    public Validator<CreateOrUpdatePaymentResponseRequest> iapValidator() {
        return new IAPValidator();
    }

    /**
     * Alipay validator.
     * @return AlipayValidator.
     */
    @Bean(name = "alipayValidator")
    public Validator<CreateOrUpdatePaymentResponseRequest> alipayValidator() {
        return new AlipayValidator();
    }

    /**
     * Wechat validator.
     * @return WechatValidator
     */
    @Bean(name = "wechatValidator")
    public Validator<CreateOrUpdatePaymentResponseRequest> wechatValidator() {
        return new WechatValidator();
    }
}
