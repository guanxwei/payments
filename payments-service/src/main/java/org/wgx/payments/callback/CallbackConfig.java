package org.wgx.payments.callback;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Callback configuration class.
 *
 */
@Configuration
public class CallbackConfig {

    @Resource
    private Environment environment;

    /**
     * Callback default bean definition, update here when the callback module is ready.
     * @return Callback implementation.
     */
    @Bean(name = "callback")
    public Callback callback() {
        return new CallbackProxy();
    }

    /**
     * Real callback cooperating with {@link CallbackProxy} to complete notification job.
     * @return Callback implementation.
     */
    @Bean(name = "realCallback")
    public Callback realCallback() {
        return new HttpBasedCallback();
    }

}
