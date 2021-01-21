package de.upb.crypto.math.random;

import java.math.BigInteger;

public class RandomGenerator {
    /**
     * If you want to exchange the implementation used to generate randomness in this library,
     * change the value of this variable.
     */
    public static RandomGeneratorImpl impl = new SecureRandomGeneratorImpl();

    private RandomGenerator() {

    }

    /**
     * Generates a uniformly random integer k with {@code 0 <= k < exclusiveUpperBound}
     */
    public static BigInteger getRandomNumber(BigInteger exclusiveUpperBound) {
        return impl.getRandomNumber(exclusiveUpperBound);
    }

    /**
     * Generates a uniformly random integer k with {@code 0 <= k < exclusiveUpperBound}
     */
    public static long getRandomNumber(long exclusiveUpperBound) {
        return getRandomNumber(BigInteger.valueOf(exclusiveUpperBound)).longValue();
    }

    /**
     * Generates a uniformly random integer k with {@code 0 <= k < exclusiveUpperBound}
     */
    public static int getRandomNumber(int exclusiveUpperBound) {
        return getRandomNumber(BigInteger.valueOf(exclusiveUpperBound)).intValue();
    }

    /**
     * Generates a uniformly random integer k with {@code inclusiveLowerBound <= k < exclusiveUpperBound}
     */
    public static BigInteger getRandomNumber(BigInteger inclusiveLowerBound, BigInteger exclusiveUpperBound) {
        return impl.getRandomNumber(inclusiveLowerBound, exclusiveUpperBound);
    }

    /**
     * Generates a uniformly random integer k with {@code inclusiveLowerBound <= k < exclusiveUpperBound}.
     */
    public static long getRandomNumber(long inclusiveLowerBound, long exclusiveUpperBound) {
        return impl.getRandomNumber(BigInteger.valueOf(inclusiveLowerBound), BigInteger.valueOf(exclusiveUpperBound)).longValue();
    }

    /**
     * Generates a uniformly random integer k with {@code inclusiveLowerBound <= k < exclusiveUpperBound}.
     */
    public static int getRandomNumber(int inclusiveLowerBound, int exclusiveUpperBound) {
        return impl.getRandomNumber(BigInteger.valueOf(inclusiveLowerBound), BigInteger.valueOf(exclusiveUpperBound)).intValue();
    }

    /**
     * Generates a uniformly random integer k with {@code 1 <= k < exclusiveUpperBound}
     */
    public static BigInteger getRandomNonZeroNumber(BigInteger exclusiveUpperBound) {
        return getRandomNumber(BigInteger.ONE, exclusiveUpperBound);
    }

    /**
     * Generates a random integer k with \(0 \leq k < 2^{\text{bitlength}}-1\).
     */
    public static BigInteger getRandomNumberOfBitlength(int bitlength) {
        return impl.getRandomNumberOfBitlength(bitlength);
    }

    /**
     * Returns random byte array of given length.
     *
     * @param l length of resulting byte array
     */
    public static byte[] getRandomBytes(int l) {
        return impl.getRandomBytes(l);
    }

    public static boolean getRandomBit() {
        return impl.getRandomBit();
    }

    /**
     * Generates a random prime number from the interval \([2^{n-1}, 2^n-1]\).
     *
     * @param bitlength desired number of bits for the prime number
     * @return a {@code BigInteger} that is probably prime
     */
    public static BigInteger getRandomPrime(int bitlength) {
        return impl.getRandomPrime(bitlength);
    }
}
