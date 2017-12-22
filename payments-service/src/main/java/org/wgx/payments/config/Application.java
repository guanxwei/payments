package org.wgx.payments.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring boot starter.
 *
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(
    basePackages = {
        "org.wgx.payments.account",
        "org.wgx.payments.controller",
        "org.wgx.payments.callback",
        "org.wgx.payments.dao",
        "org.wgx.payments.execution",
        "org.wgx.payments.controller",
        "org.wgx.payments.facade",
        "org.wgx.payments.mockbank",
        "org.wgx.payments.impl",
        "org.wgx.payments.signature",
        "org.wgx.payments.validator",
        "org.wgx.payments.config",
        "org.wgx.payments.client",
    }
)
@PropertySource({ "classpath:payments-server-${spring.profiles.active}.properties",
        "classpath:application.properties"})
@Slf4j
public class Application extends org.springframework.boot.web.support.SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    /**
     * Starter entrance.
     * @param args Start args.
     * @throws Exception Exception.
     */
    public static void main(final String[] args) throws Exception {
        if (System.getProperty("spring.profiles.active") == null) {
            log.info("Spring profiles not specified!");
            System.setProperty("spring.profiles.active", "dev");
        } else {
            log.info("Spring profiles specified by arguments!");
        }
        log.info("Spring.profiles.active = " + System.getProperty("spring.profiles.active"));
        SpringApplication.run(Application.class, args);
    }
}
