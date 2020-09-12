package de.upb.crypto.math.structures.groups.exp;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * A class containing multi-exponentiation algorithms.
 *
 * @author Raphael Heitjohann, taken from Swante Scholz' implementation.
 */
public class ExponentiationAlgorithms {

    /**
     * Evaluates a multi-exponentiation using simultaneous sliding window approach. Uses power
     * products. Only useful for higher number of bases if the power products are cached as
     * computing power products for more than ~10 basis very expensive. Cached power products
     * can also not necessarily be reused in other multi-exponentiation as they only work
     * for that specific set of bases.
     * @param multiExpContext Multi-exponentiation to evaluate.
     * @param windowSize Window size for power products.
     * @param enableCaching Whether to cache power products.
     * @return Result of multi-exponentiation.
     */
    /*public static GroupElement simultaneousSlidingWindowMulExp(MultiExpContext multiExpContext,
                                                               int windowSize, boolean enableCaching) {
        List<GroupElement> bases = multiExpContext.getBases();
        List<BigInteger> exponents = multiExpContext.getExponents();
        List<GroupElement> powerProducts;
        if (enableCaching) {
            GroupPrecomputationsFactory.GroupPrecomputations groupPrecomputations =
                    GroupPrecomputationsFactory.get(bases.get(0).getStructure());
            powerProducts = groupPrecomputations.getPowerProducts(bases, windowSize);
        } else {
            powerProducts = computePowerProducts(bases, windowSize);
        }
        int numBases = bases.size();

        GroupElement A = bases.get(0).getStructure().getNeutralElement();
        int j = getLongestExponentBitLength(exponents) - 1;
        while (j >= 0) {
            final int finalj = j;
            boolean anyMatch = false;
            for (int i = 0; i < numBases; ++i) {
                anyMatch |= exponents.get(i).testBit(finalj);
            }
            if (!anyMatch) {
                A = A.square();
                j--;
            } else {
                int jNew = Math.max(j - windowSize, -1);
                int J = jNew + 1;

                while (true) {
                    final int finalJ = J;
                    anyMatch = false;
                    for (int i = 0; i < numBases; ++i) {
                        anyMatch |= exponents.get(i).testBit(finalJ);
                    }
                    if (anyMatch) {
                        break;
                    }
                    J++;
                }
                int e = 0;
                for (int i = numBases - 1; i >= 0; i--) {
                    int ePart = 0;
                    for (int k = j; k >= J; k--) {
                        ePart <<= 1;
                        if (exponents.get(i).testBit(k)) {
                            ePart++;
                        }
                    }
                    e <<= windowSize;
                    e |= ePart;
                }
                while (j >= J) {
                    A = A.square();
                    j--;
                }
                A = A.op(powerProducts.get(e));
                while (j > jNew) {
                    A = A.square();
                    j--;
                }
            }
        }
        return A;
    }*/

    /**
     * Compute power products for bases with window size. A power product is a term of the form
     * e.g. (for 3 bases) T_{i,j,k} = b_1^i * b_2^j * b_3^k. This function computes all
     * power products for the given window size. Used by simultaneous multi-exponentiation alg.
     * @param bases List of bases to compute power products for.
     * @param windowSize Limit on powers to compute.
     * @return List of power products.
     */
    /*private static List<GroupElement> computePowerProducts(List<GroupElement> bases, int windowSize) {
        int numPrecomputedPowers = 1 << (windowSize * bases.size());
        List<GroupElement> powerProducts = new ArrayList<>(numPrecomputedPowers);
        Group group = bases.get(0).getStructure();
        // prefill arraylist
        for (int i = 0; i < numPrecomputedPowers; ++i) {
            powerProducts.add(group.getNeutralElement());
        }

        powerProducts.set(0, group.getNeutralElement());
        for (int i = 1; i < (1 << windowSize); i++) {
            powerProducts.set(i, powerProducts.get(i-1).op(bases.get(0)));
        }
        for (int b = 1; b < bases.size(); b++) {
            int shift = windowSize * b;
            for (int e = 1; e < (1 << windowSize); e++) {
                int eShifted = e << shift;
                int previousEShifted = (e - 1) << shift;
                for (int i = 0; i < (1 << shift); i++) {
                    powerProducts.set(
                            eShifted + i,
                            powerProducts.get(previousEShifted + i).op(bases.get(b))
                    );
                }
            }
        }
        return powerProducts;
    }*/

    /**
     * Evaluates a multi-exponentiation using the interleaved sliding window algorithm.
     * Powers are computed per basis, instead of for all basis together like for the simultaneous
     * approach. This means that cached powers can be reused in other multi-exponentiations, and,
     * for a large amount of bases, the precomputation is a lot cheaper than in the simultaneous
     * approach.
     */
    public static GroupElementImpl interleavingSlidingWindowMultiExp(Multiexponentiation multiexp, int windowSize) {
        multiexp.ensurePrecomputation(windowSize);
        List<MultiExpTerm> terms = multiexp.getTerms();
        if (terms.isEmpty()) //nothing to do here.
            return multiexp.getConstantFactor().orElseThrow(() -> new IllegalArgumentException("Cannot compute an empty multiexp"));
        int numTerms = terms.size();

        // we are assuming that every base has same underlying group
        GroupElementImpl result = terms.get(0).getBase().getStructure().getNeutralElement();
        int longestExponentBitLength = terms.stream().mapToInt(t -> t.getExponent().bitLength()).max().getAsInt();
        int[] windowPos = new int[numTerms]; //position of the (sliding) window for each base. -1 signifies next window position must be computed.
        Arrays.fill(windowPos, -1);
        int[] windowVal = new int[numTerms]; //the exponent of the current window.

        for (int j = longestExponentBitLength - 1; j >= 0; j--) {
            if (j != longestExponentBitLength - 1) {
                result = result.square();
            }
            for (int i = 0; i < numTerms; i++) { //for each term
                BigInteger exponent = terms.get(i).getExponent();
                if (windowPos[i] == -1 && exponent.testBit(j)) { //start a new window
                    int J = j - windowSize + 1;
                    while (!testBit(exponent, J)) {
                        J++;
                    }
                    windowPos[i] = J;
                    windowVal[i] = 0;
                    for (int k = j; k >= J; k--) {
                        windowVal[i] <<= 1;
                        if (testBit(exponent, k)) {
                            windowVal[i]++; //TODO e[i] = e[i] | 1 ?
                        }
                    }
                } //now wait for the window position to occur through the squaring steps
                if (windowPos[i] == j) { //found window position. Multiply the whole thing with base^windowVal
                    result = result.op(terms.get(i).getPrecomputation().get(windowVal[i])); //TODO was: oddPowers.get(i).get(windowVal[i] / 2)
                    windowPos[i] = -1;
                }
            }
        }

        //Multiply with constant specified in the Multiexponentiation
        result = multiexp.getConstantFactor().map(result::op).orElse(result);

        return result;
    }

    /**
     * Evaluates a multi-exponentiation using an interleaved WNAF-bases algorithm. Useful in groups
     * where inversion is as cheap or cheaper than the group operation itself, such as elliptic
     * curves.
     */
    public static GroupElementImpl interleavingWnafMultiExp(Multiexponentiation multiexp, int windowSize) {
        multiexp.ensurePrecomputation(windowSize); //TODO choose larger windowSize if possible, e.g., all bases have had more precomputation anyway? Add setting for "usual window size" and "precomputation window size". maxExp = Math.max(windowSize, multiexp.minPrecomputedPower); Need to adapt windowSize
        List<MultiExpTerm> terms = multiexp.getTerms();
        if (terms.isEmpty()) //nothing to do here.
            return multiexp.getConstantFactor().orElseThrow(() -> new IllegalArgumentException("Cannot compute an empty multiexp"));

        int longestExponentDigitLength = 0; //TODO leading zeros are just a bunch of neutral element operations, so just choose a maximum length and go with that instead of copying arrays around so many times (once in precomputeExponentForWnaf and then again here for padding)
        int[][] exponentDigits = new int[terms.size()][];
        for (int i = 0; i < terms.size(); ++i) {
            exponentDigits[i] = precomputeExponentDigitsForWnaf(terms.get(i).exponent, windowSize);
            longestExponentDigitLength = Math.max(longestExponentDigitLength, exponentDigits[i].length);
        }
        // padding with zeros
        for (int i = 0; i < exponentDigits.length; ++i) {
            int[] paddedArray = new int[longestExponentDigitLength];
            System.arraycopy(exponentDigits[i], 0, paddedArray, 0, exponentDigits[i].length);
            exponentDigits[i] = paddedArray;
        }
        // now evaluate
        GroupImpl group = terms.get(0).base.getStructure();
        GroupElementImpl result = group.getNeutralElement();
        for (int j = longestExponentDigitLength - 1; j >= 0; j--) {
            if (j != longestExponentDigitLength - 1) {
                result = result.square();
            }
            for (int i = 0; i < exponentDigits.length; i++) {
                int exponentDigit = exponentDigits[i][j];
                if (exponentDigit != 0) {
                    result = result.op(terms.get(i).getPrecomputation().get(exponentDigit));
                }
            }
        }

        //Multiply with constant specified in the Multiexponentiation
        result = multiexp.getConstantFactor().map(result::op).orElse(result);

        return result;
    }

    /**
     * Tests if the bit at position {@code index} equals {@code 1}, i.e. is set.
     * Compared to {@link BigInteger#testBit(int)} also supports a negative index (then always returns {@code false}).
     * @param n The number to test the bit of.
     * @param index The index of the bit to test.
     * @return {@code true} if the bit at position {@code index} is set, {@code false} if not.
     */
    private static boolean testBit(BigInteger n, int index) {
        if (index < 0) {
            return false;
        }
        return n.testBit(index);
    }

    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes this.inv().pow(-k).
     */
    public static GroupElementImpl binSquareMultiplyExp(GroupElementImpl base, BigInteger k) { //default implementation: square&multiply algorithm
        if (k.signum() < 0)
            return binSquareMultiplyExp(base, k.negate()).inv();

        GroupElementImpl result = base.getStructure().getNeutralElement();
        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = result.op(result);
            if (k.testBit(i))
                result = result.op(base);
        }
        return result;
    }

    public static GroupElementImpl slidingWindowExp(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation, int windowSize) {
        //TODO in this and other algorithms: what happens with negative exponents? Or are those just forbidden?
        if (precomputation == null)
            precomputation = new SmallExponentPrecomputation(base);
        else
            windowSize = Math.max(precomputation.getCurrentSupportedWindowSize(), windowSize);

        precomputation.compute(windowSize);

        // we are assuming that every base has same underlying group
        GroupElementImpl result = base.getStructure().getNeutralElement();
        int exponentBitlen = exponent.bitLength(); //TODO maybe skip this and always set it to log p?
        int windowPos = -1; //position of the (sliding) window. -1 signifies next window position must be computed.
        int windowVal = 0; //the exponent of the current window.

        for (int j = exponentBitlen - 1; j >= 0; j--) {
            if (j != exponentBitlen - 1) {
                result = result.square();
            }

            if (windowPos == -1 && exponent.testBit(j)) { //start a new window
                int J = j - windowSize + 1;
                while (!testBit(exponent, J)) {
                    J++;
                }
                windowPos = J;
                windowVal = 0;
                for (int k = j; k >= J; k--) {
                    windowVal <<= 1;
                    if (testBit(exponent, k)) {
                        windowVal++;
                    }
                }
            } //now wait for the window position to occur through the squaring steps
            if (windowPos == j) { //found window position. Multiply the whole thing with base^windowVal
                result = result.op(precomputation.get(windowVal));
                windowPos = -1;
            }
        }

        return result;
    }

    public static GroupElementImpl wnafExp(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation, int windowSize) {
        //TODO in this and other algorithms: what happens with negative exponents? Or are those just forbidden?
        if (precomputation == null)
            precomputation = new SmallExponentPrecomputation(base);
        else
            windowSize = Math.max(precomputation.getCurrentSupportedWindowSize(), windowSize);

        int maxExp = (1 << windowSize) - 1;
        precomputation.compute(windowSize);

        int[] exponentDigits = precomputeExponentDigitsForWnaf(exponent, windowSize);
        int exponentDigitsLen = exponentDigits.length;

        // now evaluate
        GroupImpl group = base.getStructure();
        GroupElementImpl result = group.getNeutralElement();
        for (int j = exponentDigitsLen - 1; j >= 0; j--) {
            if (j != exponentDigitsLen - 1) {
                result = result.square();
            }
            int exponentDigit = exponentDigits[j];
            if (exponentDigit != 0) {
                result = result.op(precomputation.get(exponentDigit));
            }
        }

        return result;
    }

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
        boolean invertEverything = false;
        if (exponent.signum() < 0) {
            invertEverything = true;
            exponent = exponent.negate();
        }

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
            bi[i] = invertEverything ? -b : b;
            i++;
            c = c.shiftRight(1);
        }
        int[] bWithoutLeadingZeros = new int[i];
        System.arraycopy(bi, 0, bWithoutLeadingZeros, 0, i);
        return bWithoutLeadingZeros;
    }
}
