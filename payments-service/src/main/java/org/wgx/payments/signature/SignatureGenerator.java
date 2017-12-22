package org.wgx.payments.signature;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * Signature generators.
 */
public enum SignatureGenerator {

    /**
     * Digital signature generator.
     * Signature length: 10
     */
    BASE_10 {
        @Override
        public String generate() {
            return RandomStringUtils.random(10, "0123456789");
        }
    },

    /**
     * Digital signature generator.
     * Signature length: 20
     */
    BASE_20 {
        @Override
        public String generate() {
            return RandomStringUtils.random(20, "0123456789");
        }
    },

    /**
     * Digital signature generator.
     * Signature length: 32
     */
    BASE_32 {
        @Override
        public String generate() {
            String prefix = date();
            StringBuilder sb = new StringBuilder(32);
            sb.append(prefix).append(RandomStringUtils.random(15, "0123456789"));
            return sb.toString();
        }
    },

    /**
     * Alphabetic signature generator.
     * Signature length: 10.
     */
    ALPHA_10 {
        @Override
        public String generate() {
            return RandomStringUtils.randomAlphabetic(10);
        }
    },

    /**
     * Alphabetic signature generator.
     * Signature length: 20.
     */
    ALPHA_20 {
        @Override
        public String generate() {
            return RandomStringUtils.randomAlphabetic(20);
        }
    },

    /**
     * Alphanumeric signature generator.
     * Signature length: 10.
     */
    MIXED_10 {
        @Override
        public String generate() {
            return RandomStringUtils.randomAlphanumeric(10);
        }
    },

    /**
     * Alphanumeric signature generator.
     * Signature length: 20.
     */
    MIXED_20 {
        @Override
        public String generate() {
            return RandomStringUtils.randomAlphanumeric(20);
        }
    };

    private static final String FORMAT = "yyyyMMddHHmmssSSS";

    private static String date() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formater = new SimpleDateFormat(FORMAT);
        return formater.format(date);
    }

    /**
     * Generate signature according to rules.
     * @return Randomly generated signature.
     */
    public abstract String generate();

}
