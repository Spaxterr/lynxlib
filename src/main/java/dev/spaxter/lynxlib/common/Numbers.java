package dev.spaxter.lynxlib.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Number utilities.
 */
public class Numbers {
    /**
     * Round a float to the given decimal count.
     *
     * @param input    Float to round
     * @param decimals Number of decimals
     * @return Rounded float value
     */
    public static float round(float input, int decimals) {
        BigDecimal decimal = new BigDecimal(input).setScale(decimals, RoundingMode.HALF_UP);
        return decimal.floatValue();
    }
}
