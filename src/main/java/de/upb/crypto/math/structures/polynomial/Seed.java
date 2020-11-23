package de.upb.crypto.math.structures.polynomial;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/**
 * Represents a bit string that can be used to initialize a polynomial via
 * {@link PolynomialRing.Polynomial#Polynomial(Seed)}.
 */
public class Seed {

    private byte[] internalSeed;

    private int bitLength;

    private int usedBits = 0;

    /**
     * Takes a byte array as a seed. The bitLength of this seed is the length of
     * the array times 8 (i.e. every bit is interpreted as a bit of the seed).
     *
     * @param seed the seed that should be encapsulated
     */
    public Seed(byte[] seed) {
        internalSeed = Arrays.copyOf(seed, seed.length);
        bitLength = seed.length * 8;
    }

    /**
     * Interprets the byte array representation of a BigInteger as a seed.
     *
     * @param seed
     */
    public Seed(BigInteger seed) {
        this(seed.toByteArray());
    }

    /**
     * Creates a new seed consisting of bitLength bytes
     *
     * @param bitLength the length of the seed
     */
    public Seed(int bitLength) {
        this(new Random(), bitLength);
    }

    /**
     * Interprets the first bitlength bits of this seed as the seed.
     *
     * @param seed      the seed
     * @param bitLength the length of the seed
     */
    public Seed(byte[] seed, int bitLength) {
        this(seed, 0, bitLength);
    }

    /**
     * @param seed
     * @param usedBits
     * @param bitLength
     */
    public Seed(byte[] seed, int usedBits, int bitLength) {

        this.usedBits = usedBits;
        this.bitLength = bitLength;
        internalSeed = Arrays.copyOf(seed, seed.length);
    }

    public Seed(Random random, int bitLength) {
        this.bitLength = bitLength;
        byte[] seed = new byte[(int) Math.ceil((double) bitLength / 8)];

        random.nextBytes(seed);
        internalSeed = seed;
    }

    public int getBitLength() {
        return bitLength;
    }

    public int getBitAt(int index) {
        int actualIndex = index + usedBits;
        int bytePos = actualIndex / 8;
        int offset = actualIndex % 8;
        return (internalSeed[bytePos] >> offset & 1);
    }

    public int getByteLength() {
        return internalSeed.length;
    }

    public byte[] getInternalSeed() {
        return internalSeed;
    }

    @Override
    public String toString() {
        return "Seed [internalSeed=" + Arrays.toString(internalSeed) + ", bitLength=" + bitLength + ", usedBits=" + usedBits + "]";
    }


}
