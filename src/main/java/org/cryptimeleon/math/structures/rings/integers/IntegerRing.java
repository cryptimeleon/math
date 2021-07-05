package org.cryptimeleon.math.structures.rings.integers;

import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * The ring of integers \(\mathbb{Z}\).
 */
public class IntegerRing implements Ring {

    public IntegerRing() {

    }

    public IntegerRing(Representation repr) {
        //No representation needed
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return null; //infinite
    }

    @Override
    public Representation getRepresentation() {
        return null; //not necessary - no parameters
    }

    @Override
    public BigInteger sizeUnitGroup() throws UnsupportedOperationException {
        return BigInteger.valueOf(2); //{-1, 1}
    }

    @Override
    public IntegerElement getZeroElement() {
        return new IntegerElement(BigInteger.ZERO);
    }

    @Override
    public IntegerElement getOneElement() {
        return new IntegerElement(BigInteger.ONE);
    }

    @Override
    public IntegerElement getElement(BigInteger i) {
        return new IntegerElement(i);
    }

    @Override
    public double estimateCostInvPerOp() {
        return 6;
    }

    @Override
    public double estimateCostNegPerOp() {
        return 2;
    }

    @Override
    public IntegerElement restoreElement(Representation repr) {
        return new IntegerElement(repr.bigInt().get());
    }

    @Override
    public IntegerElement getUniformlyRandomElement() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("You cannot draw uniformly from an infinite set");
    }

    @Override
    public RingElement getUniformlyRandomUnit() throws UnsupportedOperationException {
        return new IntegerElement(BigInteger.valueOf(-1).pow(RandomGenerator.getRandomNumber(BigInteger.valueOf(2)).intValue()));
    }

    @Override
    public BigInteger getCharacteristic() {
        return BigInteger.ZERO;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IntegerRing;
    }

    @Override
    public int hashCode() {
        return 7;
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return Optional.empty();
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    /**
     * Decomposes a given number into digits with the given base.
     * <p>
     * For example, for base = 2, this does bit decomposition.
     *
     * @return an array {@code A} containing values {@code A[i] < base} such that
     *      \(\sum_i{ \text{A}[i] \cdot \text{base}^i} = \text{number}\)
     */
    public static BigInteger[] decomposeIntoDigits(BigInteger number, BigInteger base) {
        if (base.signum() <= 0 || number.signum() < 0)
            throw new IllegalArgumentException("Parameters must be positive/non-negative");

        ArrayList<BigInteger> result = new ArrayList<>();
        BigInteger remainingDigits = number;

        while (remainingDigits.signum() != 0) {
            BigInteger[] div = remainingDigits.divideAndRemainder(base);
            result.add(div[1]); //current digit
            remainingDigits = div[0]; // remaining digits
        }

        return result.toArray(new BigInteger[0]);
    }

    /**
     * Decomposes a given number into the given number of digits with the given base.
     * <p>
     * For example, for base = 2, this does bit decomposition.
     *
     * @return an array {@code A} containing values {@code A[i] < base} such that
     *      \(\sum_i{ \text{A}[i] \cdot \text{base}^i} = \text{number}\)
     * @throws IllegalArgumentException if {@code numDigits} is not enough to represent the given number
     */
    public static BigInteger[] decomposeIntoDigits(BigInteger number, BigInteger base, int numDigits) {
        if (base.signum() <= 0 || number.signum() < 0 || numDigits < 0)
            throw new IllegalArgumentException("Parameters must be positive/non-negative");

        BigInteger[] result = new BigInteger[numDigits];
        BigInteger remainingDigits = number;

        for (int j=0; j < numDigits; j++) {
            BigInteger[] div = remainingDigits.divideAndRemainder(base);
            result[j] = div[1]; //current digit
            remainingDigits = div[0]; // remaining digits
        }

        if (remainingDigits.signum() != 0)
            throw new IllegalArgumentException("Unable to represent " + number + " base " + base + " with " + numDigits + " digits");

        return result;
    }

    /**
     * Decomposes a given number into digits with the given base.
     * <p>
     * For example, for base = 2, this does bit decomposition.
     *
     * @return an array {@code A} containing values {@code A[i] < base} such that
     *         \(\sum_i{ \text{A}[i] \cdot \text{base}^i} = \text{number}\)
     */
    public static BigInteger[] decomposeIntoDigits(BigInteger number, long base) {
        return decomposeIntoDigits(number, BigInteger.valueOf(base));
    }

    /**
     * Decomposes a given number into the given number of digits with the given base.
     * <p>
     * For example, for base = 2, this does bit decomposition.
     *
     * @return an array {@code A} containing values {@code A[i] < base} such that
     *         \(\sum_i{ \text{A}[i] \cdot \text{base}^i} = \text{number}\)
     * @throws IllegalArgumentException if {@code numDigits} is not enough to represent the given number
     */
    public static BigInteger[] decomposeIntoDigits(BigInteger number, long base, int numDigits) {
        return decomposeIntoDigits(number, BigInteger.valueOf(base), numDigits);
    }
}
