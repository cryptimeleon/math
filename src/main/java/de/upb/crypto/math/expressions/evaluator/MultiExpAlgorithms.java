package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory;
import de.upb.crypto.math.interfaces.structures.UncachedGroupPrecomputations;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A class containing multi-exponentiation algorithms.
 *
 * @author Raphael Heitjohann, taken from Swante Scholz' implementation.
 */
public class MultiExpAlgorithms {

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
    public static GroupElement simultaneousSlidingWindowMulExp(MultiExpContext multiExpContext,
                                                         int windowSize, boolean enableCaching) {
        // TODO: we should not do any precomputations for bases with exponents 1
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
            if (IntStream.range(0, numBases)
                    .noneMatch(it -> exponents.get(it).testBit(finalj))) {
                A = A.op(A);
                j--;
            } else {
                int jNew = Math.max(j - windowSize, -1);
                int J = jNew + 1;

                while (true) {
                    final int finalJ = J;
                    if (IntStream.range(0, numBases)
                            .anyMatch(it -> exponents.get(it).testBit(finalJ))) {
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
                    A = A.op(A);
                    j--;
                }
                A = A.op(powerProducts.get(e));
                while (j > jNew) {
                    A = A.op(A);
                    j--;
                }
            }
        }
        return A;
    }

    /**
     * Compute power products for bases with window size. A power product is a term of the form
     * e.g. (for 3 bases) T_{i,j,k} = b_1^i * b_2^j * b_3^k. This function computes all
     * power products for the given window size. Used by simultaneous multi-exponentiation alg.
     * @param bases List of bases to compute power products for.
     * @param windowSize Limit on powers to compute.
     * @return List of power products.
     */
    private static List<GroupElement> computePowerProducts(List<GroupElement> bases, int windowSize) {
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
    }

    /**
     * Evaluates a multi-exponentiation using the interleaved sliding window algorithm.
     * Powers are computed per basis, instead of for all basis together like for the simultaneous
     * approach. This means that cached powers can be reused in other multi-exponentiations, and,
     * for a large amount of bases, the precomputation is a lot cheaper than in the simultaneous
     * approach.
     * @param multiExpContext Multi-exponentiation to evaluate.
     * @param windowSize Window size for precomputed odd powers.
     * @param enableCaching Whether to cache precomputed odd powers.
     * @return Result of multi-exponentiation.
     */
    public static GroupElement interleavingSlidingWindowMultiExp(MultiExpContext multiExpContext,
                                                           int windowSize, boolean enableCaching) {
        List<GroupElement> bases = multiExpContext.getBases();
        List<BigInteger> exponents = multiExpContext.getExponents();
        List<List<GroupElement>> oddPowers = new ArrayList<>();
        int maxExp = (1 << windowSize) - 1;
        if (enableCaching) {
            GroupPrecomputationsFactory.GroupPrecomputations groupPrecomputations =
                    GroupPrecomputationsFactory.get(bases.get(0).getStructure());
            for (GroupElement base : bases) {
                oddPowers.add(groupPrecomputations.getOddPowers(base, maxExp));
            }
        } else {
            for (GroupElement base : bases) {
                oddPowers.add(UncachedGroupPrecomputations
                        .precomputeSmallOddPowers(base, maxExp));
            }
        }
        int numBases = bases.size();

        // we are assuming that every base has same underlying group
        GroupElement A = bases.get(0).getStructure().getNeutralElement();
        int longestExponentBitLength = getLongestExponentBitLength(exponents);
        int[] wh = new int[numBases];
        int[] e = new int[numBases];
        for (int i = 0; i < numBases; i++) {
            wh[i] = -1;
        }
        for (int j = longestExponentBitLength - 1; j >= 0; j--) {
            if (j != longestExponentBitLength - 1) {
                A = A.op(A);
            }
            for (int i = 0; i < numBases; i++) {
                if (wh[i] == -1 && exponents.get(i).testBit(j)) {
                    int J = j - windowSize + 1;
                    while (!testBit(exponents.get(i), J)) {
                        J++;
                    }
                    wh[i] = J;
                    e[i] = 0;
                    for (int k = j; k >= J; k--) {
                        e[i] <<= 1;
                        if (testBit(exponents.get(i), k)) {
                            e[i]++;
                        }
                    }
                }
                if (wh[i] == j) {
                    A = A.op(oddPowers.get(i).get(e[i] / 2));
                    wh[i] = -1;
                }
            }
        }
        return A;
    }

    /**
     * Evaluates a multi-exponentiation using an interleaved WNAF-bases algorithm. Useful in groups
     * where inversion is as cheap or cheaper than the group operation itself, such as elliptic
     * curves.
     * @param multiExpContext Multi-exponentiation to evaluate.
     * @param windowSize Window size for precomputed odd powers.
     * @param enableCaching Whether to cache precomputed odd powers.
     * @return Result of multi-exponentiation.
     */
    public static GroupElement interleavingWnafMultiExp(MultiExpContext multiExpContext, int windowSize,
                                                        boolean enableCaching) {
        List<GroupElement> bases = multiExpContext.getBases();
        List<BigInteger> exponents = multiExpContext.getExponents();
        List<List<GroupElement>> oddPowers = new ArrayList<>();
        int maxExp = (1 << windowSize) - 1;
        if (enableCaching) {
            GroupPrecomputationsFactory.GroupPrecomputations groupPrecomputations =
                    GroupPrecomputationsFactory.get(bases.get(0).getStructure());
            for (GroupElement base : bases) {
                oddPowers.add(groupPrecomputations.getOddPowers(base, maxExp));
            }
        } else {
            for (GroupElement base : bases) {
                oddPowers.add(UncachedGroupPrecomputations.precomputeSmallOddPowers(base, maxExp));
            }
        }
        int longestExponentDigitLength = 0;
        int[][] exponentDigits = new int[bases.size()][];
        for (int i = 0; i < bases.size(); ++i) {
            exponentDigits[i] = UncachedGroupPrecomputations.precomputeExponentDigitsForWNAF(
                    exponents.get(i),
                    windowSize
            );
            longestExponentDigitLength = Math.max(
                    longestExponentDigitLength,
                    exponentDigits[i].length
            );
        }
        // padding with zeros
        for (int i = 0; i < bases.size(); ++i) {
            int[] paddedArray = new int[longestExponentDigitLength];
            System.arraycopy(exponentDigits[i], 0, paddedArray, 0, exponentDigits[i].length);
            exponentDigits[i] = paddedArray;
        }
        // now evaluate
        Group group = bases.get(0).getStructure();
        GroupElement A = group.getNeutralElement();
        for (int j = longestExponentDigitLength - 1; j >= 0; j--) {
            if (j != longestExponentDigitLength - 1) {
                A = A.square();
            }
            for (int i = 0; i < bases.size(); i++) {
                int exponentDigit = exponentDigits[i][j];
                if (exponentDigit != 0) {
                    GroupElement power = oddPowers.get(i).get(Math.abs(exponentDigit) / 2);
                    if (exponentDigit < 0) {
                        power = power.inv();
                    }
                    A = A.op(power);
                }
            }
        }
        return A;
    }

    private static boolean testBit(BigInteger n, int index) {
        if (index < 0) {
            return false;
        }
        return n.testBit(index);
    }

    private static int getLongestExponentBitLength(List<BigInteger> exponents) {
        int max = 1;
        for (BigInteger exp : exponents) {
            if (exp.bitLength() > max) {
                max = exp.bitLength();
            }
        }
        return max;
    }

}
