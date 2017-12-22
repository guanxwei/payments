package org.wgx.payments.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/***
 * IAP processor constants holder.
 *
 */
public final class IAPConstants {

    private IAPConstants() { }

    /**
     * Sand box url.
     */
    public static final String SAND_BOX_URL = "https://sandbox.itunes.apple.com/verifyReceipt";

    /**
     * Non sand box url.
     */
    public static final String ONLINE_URL = "https://buy.itunes.apple.com/verifyReceipt";

    /**
     * Charset.
     */
    public static final String CHARSET = "utf-8";

    /**
     * Default applied IAP verify url, will be automatically initiateD according to environment setting.
     */
    public static final String VERIFY_URL;

    static {
        String profile = System.getProperty("spring.profiles.active");
        Properties properties = new Properties();
        try (InputStream input = Constants.class.getResourceAsStream(String.format("/payments-server-%s.properties", profile))) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        VERIFY_URL = properties.getProperty("iap.verify.url");
    }
}
