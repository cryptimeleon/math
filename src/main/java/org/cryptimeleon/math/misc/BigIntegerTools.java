package org.cryptimeleon.math.misc;

import java.math.BigInteger;

public abstract class BigIntegerTools {

    /**
     * Returns the integer value corresponding to the given {@link BigInteger}, throwing a {@link ArithmeticException}
     * if the value is out of bounds.
     * <p>
     * Implements the {@code intValueExact} method of {@code BigInteger} that is missing in the Android Sdk.
     * @param num the {@code BigInteger} to convert
     * @return the integer with the same value as {@code num}
     *
     * @throws ArithmeticException if the value of {@code num} is outside the supported integer values
     */
    public static Integer getExactInt(BigInteger num) {
        if (num.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0
                || num.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0)
            throw new ArithmeticException("Integer value of BigInteger " + num + " is out of integer range");
        return num.intValue();
    }
}
