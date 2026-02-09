package com.dev.ultron.utilitarios;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {

    private BigDecimalUtil() {
        // Private constructor
    }

    public static final BigDecimal PERCENTAGE_100 = new BigDecimal("100.00");

    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        if (dividend == null || divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return dividend.divide(divisor, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {
        if (amount == null || percentage == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(percentage).divide(PERCENTAGE_100, 2, RoundingMode.HALF_UP);
    }
}
