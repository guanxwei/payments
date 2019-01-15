package org.wgx.payments.utils;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Utility class provides some useful methods to process money related stuffs.
 * @author weigu
 *
 */
public final class AmountUtils {

    private AmountUtils() { }

    public static BigDecimal total(final Collection<String> amounts) {
        if (amounts == null || amounts.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return amounts.parallelStream()
                .map(AmountUtils::from)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal from(final String amount) {
        return new BigDecimal(amount);
    }
}
