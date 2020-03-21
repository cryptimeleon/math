package de.upb.crypto.math.interfaces.structures;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Helper methods for precomputations of uncached single exponentiation algorithms.
 *
 * @author Raphael Heitjohann
 */
public abstract class UncachedGroupPrecomputations {

    /**
     * @return Lowest n bits of i. Works for all n < 32.
     */
    public static int getNLeastSignificantBits(int i, int numberOfLowBits) {
        return i & ((1 << numberOfLowBits) - 1);
    }

    /**
     * Prepares WNAF representation (see master thesis by Swante Scholz) of exponent.
     * @param exponent The exponent to compute WNAF representation for.
     * @param windowSize The window size to use. This determines width of the WNAF representation.
     * @return Array of exponent digits in WNAF form.
     */
    public static int[] precomputeExponentDigitsForWnaf(BigInteger exponent, int windowSize) {
        BigInteger c = exponent;
        int[] bi = new int[exponent.bitLength()+1];
        int i = 0;
        while (c.signum() > 0) {
            int b = 0;
            if (c.testBit(0)) {
                b = getNLeastSignificantBits(c.intValue(), windowSize+1);
                if (b >= 1 << windowSize) {
                    b -= 1 << (windowSize+1);
                }
                c = c.subtract(BigInteger.valueOf(b));
            }
            bi[i] = b;
            i++;
            c = c.shiftRight(1);
        }
        int[] bWithoutLeadingZeros = new int[i];
        System.arraycopy(bi, 0, bWithoutLeadingZeros, 0, i);
        return bWithoutLeadingZeros;
    }

    /**
     * Precomputes odd powers of the given base up to the given maximum exponent.
     * @param base Base to compute odd powers of.
     * @param maxExp Maximum exponent to exponentiate the base with. Does not have to be uneven,
     *               but algorithm will always only exponentiate up to largest
     *               uneven power <= maxExp.
     * @return List of odd powers sorted ascending by exponent.
     */
    public static List<GroupElement> precomputeSmallOddPowers(GroupElement base, int maxExp) {
        List<GroupElement> res = new ArrayList<>();
        res.add(base);
        GroupElement square = base.square();
        for (int i = 1; i < (maxExp+1)/2; i++) {
            res.add(res.get(i-1).op(square));
        }
        return res;
    }
}
