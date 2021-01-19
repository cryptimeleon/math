package de.upb.crypto.math.random.interfaces;

import java.math.BigInteger;

/**
 * Interface for classes that are able to supply randomness.
 */
public interface RandomGenerator {
    /**
     * Retrieves the next random bit.
     * @return the next random bit
     */
    boolean nextBit();

    /**
     * Sets the seed for this random generator.
     * @param seed the seed to use
     */
    void setSeed(BigInteger seed);

    /**
     * Generates a random number between {@code 0} and {@code l-1} (inclusive).
     *
     * @param l the upper bound (exclusive)
     * @return a uniformly distributed number between {@code 0} and {@code length-1}
     */
    default BigInteger getRandomElement(BigInteger l) {
        return RandomUtil.getRandomElement(this, l);
    }

    /**
     * Generates a random number between \(0\) and \(2^{\text{length-1}}\) (inclusive).
     *
     * @param length maximum length in bits of the chosen number
     * @return a uniformly distributed random number between \(0\) and \(2^{\text{length-1}}\) (inclusive)
     */
    default BigInteger getRandomElement(int length) {
        return getRandomElement(BigInteger.ONE.shiftLeft(length));
    }

    /**
     * Returns random byte array of given length.
     *
     * @param l length of resulting byte array
     */
    byte[] getRandomByteArray(int l);

    /**
     * Generates a random number between {@code 1} and {@code l-1} (inclusive).
     * <p>
     * Equivalent to {@code getRandomElement(l-1)+1}.
     */
    default BigInteger getRandomNonZeroElement(BigInteger l) {
        return getRandomElement(l.subtract(BigInteger.ONE)).add(BigInteger.ONE);
    }

    /**
     * Generates a random prime number from the interval \([2^{n-1}, 2^n-1]\).
     *
     * @param n desired number of bits for the prime number
     * @return a {@code BigInteger} that is probably prime
     */
    default BigInteger getRandomPrime(int n) {
        return RandomUtil.getRandomPrime(this, n);
    }
}
