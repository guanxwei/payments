package org.wgx.payments.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Common constants holder.
 *
 */
public final class Constants {

    private Constants() { }

    /**
     * Query string seperator.
     */
    public static final String QUERYSTRING_SEPERATOR = "?";

    /**
     * The base url of the notify controler.
     */
    public static final String BASE_URL;

    static {
        String profile = System.getProperty("spring.profiles.active");
        Properties properties = new Properties();
        try (InputStream input = Constants.class.getResourceAsStream(String.format("/payments-server-%s.properties", profile))) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BASE_URL = properties.getProperty("base.url");
    }
}
