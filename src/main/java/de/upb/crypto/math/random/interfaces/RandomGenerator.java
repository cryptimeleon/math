package de.upb.crypto.math.random.interfaces;

import java.math.BigInteger;

/**
 * Interface for classes that are able to supply randomness.
 */
public interface RandomGenerator {


    boolean nextBit();

    /**
     * Generate a uniformly distributed random number between {@code 0} and {@code 2^{length-1}} (inclusive).
     *
     * @deprecated Use {@code getRandomElement} instead
     * @param length maximum length in bits of the chosen number
     * @return a uniformly distributed random number between {@code 0} and {@code 2^{length-1}} (inclusive)
     */
    @Deprecated
    default BigInteger next(int length) {
        return this.getRandomElement(length);
    }

    void setSeed(BigInteger seed);

    /**
     * Generate a uniformly distributed random number between {@code 0} and {@code length-1} (inclusive).
     *
     * @param l the upper bound (exclusive)
     * @return a uniformly distributed number between {@code 0} and {@code length-1}
     */
    default BigInteger getRandomElement(BigInteger l) {
        return RandomUtil.getRandomElement(this, l);
    }

    /**
     * Generate a uniformly distributed random number between {@code 0} and {@code 2^{length-1}} (inclusive).
     *
     * @param length maximum length in bits of the chosen number
     * @return a uniformly distributed random number between {@code 0} and {@code 2^{length-1}} (inclusive)
     */
    default BigInteger getRandomElement(int length) {
        return getRandomElement(BigInteger.ONE.shiftLeft(length));
    }

    /**
     * Return uniform byte array of given length.
     *
     * @param l length of resulting byte array
     */
    public byte[] getRandomByteArray(int l);

    /**
     * Generate a random number between {@code 1} and {@code length-1}.
     * <p>
     * Equivalent to {@code getRandomElement(l-1)+1}.
     */
    default BigInteger getRandomNonZeroElement(BigInteger l) {
        return getRandomElement(l.subtract(BigInteger.ONE)).add(BigInteger.ONE);
    }

    /**
     * Generate a (uniformly) random prime number from the interval [2^(n-1), 2^n-1].
     *
     * @param n desired number of bits for the prime number
     * @return a {@code BigInteger} that is probably prime
     */
    default BigInteger getRandomPrime(int n) {
        return RandomUtil.getRandomPrime(this, n);
    }
}
