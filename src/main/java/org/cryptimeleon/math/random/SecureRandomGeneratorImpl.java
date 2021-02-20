package org.cryptimeleon.math.random;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * A simple random generator implementation using {@link SecureRandom} instantiated from a given seed.
 */
public class SecureRandomGeneratorImpl implements RandomGeneratorImpl {
    /**
     * Java's cryptographically secure randomness generator.
     */
    protected SecureRandom rng;

    public SecureRandomGeneratorImpl() {
        rng = new SecureRandom();
    }

    @Override
    public byte[] getRandomBytes(int l) {
        byte[] result = new byte[l];
        rng.nextBytes(result);
        return result;
    }

    @Override
    public boolean getRandomBit() {
        return rng.nextBoolean();
    }

    @Override
    public BigInteger getRandomPrime(int bitlength) {
        return BigInteger.probablePrime(bitlength, rng);
    }
}