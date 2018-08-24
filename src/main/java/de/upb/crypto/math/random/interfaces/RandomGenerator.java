package de.upb.crypto.math.random.interfaces;

import java.math.BigInteger;

/**
 * Interface for classes that are able to supply randomness
 */
public interface RandomGenerator {


    boolean nextBit();

    @Deprecated
    /**
     * use getRandomElement instead
     *
     * @param length
     * @return
     */
    default BigInteger next(int length) {
        return this.getRandomElement(length);
    }

    void setSeed(BigInteger seed);

    /**
     * Generate a random number from the set {0,...,l-1}
     *
     * @param l the upper bound excluded
     * @return a uniformly distributed number from the set {0,...,l-1}
     */
    default BigInteger getRandomElement(BigInteger l) {
        return RandomUtil.getRandomElement(this, l);
    }

    /**
     * Generate a random number in {0,...,2^length -1}
     *
     * @param length
     * @return
     */
    default BigInteger getRandomElement(int length) {
        return getRandomElement(BigInteger.ONE.shiftLeft(length));
    }

    /**
     * Return uniform byte array of given length.
     *
     * @param l - length of resulting byte array
     */
    public byte[] getRandomByteArray(int l);

    /**
     * Generate a random number from the set {1,...,l-1} (isomorphic to Z_l* if l is prime)
     * Equivalent to getRandomElement(l-1)+1
     */
    default BigInteger getRandomNonZeroElement(BigInteger l) {
        return getRandomElement(l.subtract(BigInteger.ONE)).add(BigInteger.ONE);
    }

    /**
     * Generate a (uniformly) random prime number from the interval [2^(n-1), 2^n-1]
     *
     * @param n desired number of bits for the prime number
     * @return a BigInteger that is probably prime
     */
    default BigInteger getRandomPrime(int n) {
        return RandomUtil.getRandomPrime(this, n);
    }
}
