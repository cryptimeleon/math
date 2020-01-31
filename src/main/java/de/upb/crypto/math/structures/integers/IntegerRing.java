package de.upb.crypto.math.structures.integers;

import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The ring of integers. I don't think this needs any more introduction :)
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
    public IntegerElement getElement(Representation repr) {
        return new IntegerElement(repr.bigInt().get());
    }

    @Override
    public IntegerElement getUniformlyRandomElement() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("You cannot draw uniformly from an infinite set");
    }

    @Override
    public RingElement getUniformlyRandomUnit() throws UnsupportedOperationException {
        return new IntegerElement(BigInteger.valueOf(-1).pow(RandomGeneratorSupplier.getRnd().getRandomElement(BigInteger.valueOf(2)).intValue()));
    }

    @Override
    public BigInteger getCharacteristic() {
        return BigInteger.ZERO;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof IntegerRing;
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
     * For base = 2, this does bit decomposition
     *
     * @return an array A containing values A[i] < base such that Sum_i(A[i]*base^i) = number.
     */
    public static BigInteger[] decomposeIntoDigits(BigInteger number, BigInteger base) {
        int power = 0;
        BigInteger numberPowered = BigInteger.ONE;
        while (numberPowered.compareTo(number) < 0) { //as soon as number is smaller than number^power, it can be decomposed into power digits.
            numberPowered = numberPowered.multiply(number);
            power++;
        }
        return decomposeIntoDigits(number, base, power);
    }

    /**
     * Decomposes a given number into digits with the given base.
     * For base = 2, this does bit decomposition
     *
     * @return an array A of length numDigits containing values A[i] < base such that Sum_i(A[i]*base^i) = number.
     * @throws IllegalArgumentException if numDigits is not enough to represent number.
     */
    public static BigInteger[] decomposeIntoDigits(BigInteger number, BigInteger base, int numDigits) {
        if (base.signum() <= 0 || number.signum() < 0 || numDigits < 0)
            throw new IllegalArgumentException("Parameters must be positive/nonnegative");

        BigInteger[] result = new BigInteger[numDigits];
        BigInteger remainder = number;

        for (int j = numDigits - 1; j >= 0; j--) {
            BigInteger basePowJ = base.pow(j);
            BigInteger[] div = remainder.divideAndRemainder(basePowJ);
            result[j] = div[0]; //current digit
            remainder = div[1]; //remainder value (to be represented with the remaining digits)
        }

        if (remainder.signum() != 0)
            throw new IllegalArgumentException("Unable to represent "+number.toString()+" base "+base.toString()+" with "+numDigits+" digits");

        return result;
    }
}
