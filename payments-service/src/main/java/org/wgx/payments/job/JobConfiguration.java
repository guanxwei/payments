package org.wgx.payments.job;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Back-end job configuration class.
 *
 */
@Configuration
public class JobConfiguration {

    /**
     * BackendCallbackJob configuration.
     * @return BackendCallbackJob
     */
    @Bean(initMethod = "start")
    public BackendCallbackJob callbackJob() {
        return new BackendCallbackJob();
    }
}
