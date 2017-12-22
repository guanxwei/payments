package org.wgx.payments.flowcontrol;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowMonitorConfig {

    @Bean(name = "flowControlAspect")
    public FlowControlAspect flowControlAspect() {
        FlowControlAspect flowControlAspect = new FlowControlAspect();
        return flowControlAspect;
    }
}
