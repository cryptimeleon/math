package de.upb.crypto.math.structures.polynomial;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

/**
 * Represents a bit string that can be used to initialize a polynomial via
 * {@link PolynomialRing.Polynomial#Polynomial(Seed)}.
 */
public class Seed {

    /**
     * The actual seed.
     */
    protected final byte[] internalSeed;

    /**
     * The length of the seed in number of bits.
     */
    protected final int bitLength;

    /**
     *
     */
    protected int usedBits = 0;

    /**
     * Takes a byte array as a seed. The {@code bitLength} of this seed is the length of
     * the array times 8 (i.e. every bit is interpreted as a bit of the seed).
     *
     * @param seed the seed that should be encapsulated
     */
    public Seed(byte[] seed) {
        internalSeed = Arrays.copyOf(seed, seed.length);
        bitLength = seed.length * 8;
    }

    /**
     * Interprets the byte array representation of a {@code BigInteger} as a seed.
     *
     * @param seed the {@code BigInteger} to use as seed
     */
    public Seed(BigInteger seed) {
        this(seed.toByteArray());
    }

    /**
     * Creates a new seed consisting of {@code bitLength} random bytes.
     *
     * @param bitLength the length of the seed
     */
    public Seed(int bitLength) {
        this(new Random(), bitLength);
    }

    /**
     * Interprets the first {@code bitLength} bits of the given byte array as the seed.
     *
     * @param seed      the byte array to use as seed
     * @param bitLength the length of the seed
     */
    public Seed(byte[] seed, int bitLength) {
        this(seed, 0, bitLength);
    }

    /**
     * Interprets the first {@code bitLength} bits of the given byte array starting at offset {@code usedBits}
     * as the seed.
     *
     * @param seed the byte array to use as seed
     * @param usedBits the offset in the given byte array where the seed should start
     * @param bitLength the length of the seed
     */
    public Seed(byte[] seed, int usedBits, int bitLength) {
        this.usedBits = usedBits;
        this.bitLength = bitLength;
        internalSeed = Arrays.copyOf(seed, seed.length);
    }

    /**
     * Uses the given randomness to create a seed of length {@code bitLength}.
     *
     * @param random randomness for generating the new seed
     * @param bitLength the length of the new seed in number of bits
     */
    public Seed(Random random, int bitLength) {
        this.bitLength = bitLength;
        byte[] seed = new byte[(int) Math.ceil((double) bitLength / 8)];

        random.nextBytes(seed);
        internalSeed = seed;
    }

    /**
     * Returns the length of this seed in number of bits.
     */
    public int getBitLength() {
        return bitLength;
    }

    /**
     * Returns the bit at index {@code index}.
     * @param index the index of the bit to retrieve
     * @return the {@code index}'th bit
     */
    public int getBitAt(int index) {
        int actualIndex = index + usedBits;
        int bytePos = actualIndex / 8;
        int offset = actualIndex % 8;
        return (internalSeed[bytePos] >> offset & 1);
    }

    /**
     * Returns the length of the internal seed array in number of bytes.
     */
    public int getByteLength() {
        return internalSeed.length;
    }

    /**
     * Returns the internal seed byte array.
     */
    public byte[] getInternalSeed() {
        return internalSeed;
    }

    @Override
    public String toString() {
        return "Seed [internalSeed=" + Arrays.toString(internalSeed) + ", bitLength=" + bitLength + ", usedBits="
                + usedBits + "]";
    }
}
