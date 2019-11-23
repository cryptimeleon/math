package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.interfaces.structures.*;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Class for optimized evaluation of expressions. Can recognize multi-exponentiations in
 * expression trees and evaluates them using some appropriate multi-exponentiation algorithm.
 *
 * For explanation of algorithms see Swante Scholz's master thesis.
 *
 * @author Raphael Heitjohann
 */
public class OptGroupElementExpressionEvaluator implements GroupElementExpressionEvaluator {

    private boolean enableCachingInterleavedSliding;
    private boolean enableCachingSimultaneous;
    private boolean enableCachingInterleavedWnaf;
    private ForceMultiExpAlgorithmSetting forcedMultiExpAlgorithm;
    private int windowSizeInterleavedSlidingCaching;
    private int windowSizeInterleavedSlidingNoCaching;
    private int windowSizeInterleavedWnafCaching;
    private int windowSizeInterleavedWnafNoCaching;
    private int windowSizeSimultaneousCaching;
    private int windowSizeSimultaneousNoCaching;
    private int simultaneousNumBasesCutoff;
    /**
     * When to use Wnaf. Default is 100 which means that 100 inversions in the group cost as much
     * as 100 group operations. Smaller values mean that inversions are even cheaper. Wnaf will be
     * used if the groups cost of inversion is <= than this value.
     */
    private int useWnafCostInversion;

    public OptGroupElementExpressionEvaluator() {
        // TODO: best default values here?
        enableCachingInterleavedSliding = true;
        enableCachingSimultaneous = true;
        enableCachingInterleavedWnaf = true;
        forcedMultiExpAlgorithm = ForceMultiExpAlgorithmSetting.DISABLED;
        windowSizeInterleavedSlidingCaching = 8;
        windowSizeInterleavedSlidingNoCaching = 4;
        windowSizeInterleavedWnafCaching = 8;
        windowSizeInterleavedWnafNoCaching = 4;
        windowSizeSimultaneousCaching = 1;
        windowSizeSimultaneousNoCaching = 1;
        simultaneousNumBasesCutoff = 10;
        useWnafCostInversion = 100;
    }

    @Override
    public GroupElement evaluate(GroupElementExpression expr) {
        MultiExpContext multiExpContext = new MultiExpContext();
        boolean inInversion = false;
        extractMultiExpContext(expr, inInversion, multiExpContext);
        if (!multiExpContext.allBasesSameGroup()) {
            throw new IllegalArgumentException("Expression contains elements with different" +
                    "groups outside of pairings.");
        }

        return evaluateMultiExp(multiExpContext);
    }

    /**
     * Evaluate multi-exponentiation using configured/default algorithms.
     * @param multiExpContext The multiexponentiation to evaluate.
     * @return Result of evaluation of multi-exponentiation.
     */
    private GroupElement evaluateMultiExp(MultiExpContext multiExpContext) {
        switch (forcedMultiExpAlgorithm) {
            case SIMULTANEOUS:
                return simultaneousSlidingWindowMulExpWrapper(multiExpContext);
            case INTERLEAVED_SLIDING:
                return interleavingSlidingWindowMultiExpWrapper(multiExpContext);
            case INTERLEAVED_WNAF:
                return interleavingWnafWindowMultiExpWrapper(multiExpContext);
            case DISABLED:
                // select algorithm based on swante scholz's recommendations
                if (multiExpContext.getBases().size() < simultaneousNumBasesCutoff
                    && enableCachingSimultaneous) {
                    return simultaneousSlidingWindowMulExpWrapper(multiExpContext);
                } else if (multiExpContext.getBases().get(0).getStructure().estimateCostOfInvert()
                    <= useWnafCostInversion) {
                    return interleavingWnafWindowMultiExpWrapper(multiExpContext);
                } else {
                    return interleavingSlidingWindowMultiExpWrapper(multiExpContext);
                }
        }
        throw new IllegalArgumentException("Unsupported forcedMultiExpAlgorithm value");
    }

    /**
     * Compute power products for bases with window size. A power product is a term of the form
     * e.g. (for 3 bases) T_{i,j,k} = b_1^i * b_2^j * b_3^k. This function computes all
     * power products for the given window size. Used by simultaneous multi-exponentiation alg.
     * @param bases List of bases to compute power products for.
     * @param windowSize Limit on powers to compute.
     * @return List of power products.
     */
    private List<GroupElement> computePowerProducts(List<GroupElement> bases, int windowSize) {
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

    private GroupElement simultaneousSlidingWindowMulExpWrapper(MultiExpContext multiExpContext) {
        if (enableCachingSimultaneous) {
            return simultaneousSlidingWindowMulExp(
                    multiExpContext,
                    windowSizeSimultaneousCaching,
                    true
            );
        } else {
            return simultaneousSlidingWindowMulExp(
                    multiExpContext,
                    windowSizeSimultaneousNoCaching,
                    false
            );
        }
    }

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
    private GroupElement simultaneousSlidingWindowMulExp(MultiExpContext multiExpContext,
                                                         int windowSize, boolean enableCaching) {
        // TODO: we should not do any precomputations for bases with exponents 1
        List<GroupElement> bases = multiExpContext.getBases();
        List<BigInteger> exponents = multiExpContext.getExponents();
        List<GroupElement> powerProducts = new ArrayList<>();
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

    private GroupElement interleavingSlidingWindowMultiExpWrapper(MultiExpContext multiExpContext) {
        if (enableCachingInterleavedSliding) {
            return interleavingSlidingWindowMultiExp(
                    multiExpContext,
                    windowSizeInterleavedSlidingCaching,
                    true
            );
        } else {
            return interleavingSlidingWindowMultiExp(
                    multiExpContext,
                    windowSizeInterleavedSlidingNoCaching,
                    false
            );
        }
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
    private GroupElement interleavingSlidingWindowMultiExp(MultiExpContext multiExpContext,
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

    private GroupElement interleavingWnafWindowMultiExpWrapper(MultiExpContext multiExpContext) {
        if (enableCachingInterleavedWnaf) {
            return interleavingWnafMultiExp(
                    multiExpContext,
                    windowSizeInterleavedWnafCaching,
                    true
            );
        } else {
            return interleavingWnafMultiExp(
                    multiExpContext,
                    windowSizeInterleavedWnafNoCaching,
                    false
            );
        }
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
    private GroupElement interleavingWnafMultiExp(MultiExpContext multiExpContext, int windowSize,
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

    private boolean testBit(BigInteger n, int index) {
        if (index < 0) {
            return false;
        }
        return n.testBit(index);
    }

    private int getLongestExponentBitLength(List<BigInteger> exponents) {
        int max = 1;
        for (BigInteger exp : exponents) {
            if (exp.bitLength() > max) {
                max = exp.bitLength();
            }
        }
        return max;
    }

    /**
     * Takes an expression tree and flattens it into a multi-exponentiation using a simple
     * depth-first approach.
     * @param expr Expression to extract multi-exponentiation from.
     * @param inInversion Whether we currently are in an uneven level of inversion.
     * @param multiExpContext Storage for extracted multi-exponentiation.
     */
    private void extractMultiExpContext(GroupElementExpression expr, boolean inInversion,
                                        MultiExpContext multiExpContext) {
        if (expr instanceof GroupOpExpr) {
            GroupOpExpr op_expr = (GroupOpExpr) expr;
            // group not necessarily commutative, so if we are in inversion, switch order
            // TODO: this only works for multiplicative groups, other rules for additive
            if (inInversion) {
                extractMultiExpContext(op_expr.getRhs(), inInversion, multiExpContext);
                extractMultiExpContext(op_expr.getLhs(), inInversion, multiExpContext);
            } else {
                extractMultiExpContext(op_expr.getLhs(), inInversion, multiExpContext);
                extractMultiExpContext(op_expr.getRhs(), inInversion, multiExpContext);
            }
        } else if (expr instanceof GroupInvExpr) {
            extractMultiExpContext(((GroupInvExpr) expr).getBase(), !inInversion, multiExpContext);
        } else if (expr instanceof GroupPowExpr) {
            GroupPowExpr pow_expr = (GroupPowExpr) expr;
            // for now, just use evaluate naive on base and exponent
            multiExpContext.addExponentiation(
                    pow_expr.getBase().evaluate(),
                    pow_expr.getExponent().evaluate(),
                    inInversion
            );
        } else if (expr instanceof GroupElementConstantExpr) {
            // count this as basis too, multiexp algorithm can distinguish
            multiExpContext.addExponentiation(expr.evaluateNaive(), BigInteger.ONE,
                    inInversion);
        } else if (expr instanceof GroupEmptyExpr) {
            // count this as basis too, multiexp algorithm can distinguish
            multiExpContext.addExponentiation(expr.evaluateNaive(), BigInteger.ONE,
                    inInversion);
        } else if (expr instanceof PairingExpr) {
            PairingExpr pair_expr = (PairingExpr) expr;
            // TODO: Can do this in parallel
            GroupElement lhs = pair_expr.getLhs().evaluate(this);
            GroupElement rhs = pair_expr.getRhs().evaluate(this);
            GroupElement pair_result = pair_expr.getMap().apply(lhs, rhs);
            // Also use this as basis for multiexp
            multiExpContext.addExponentiation(pair_result, BigInteger.ONE, inInversion);
        } else if (expr instanceof GroupVariableExpr) {
            throw new IllegalArgumentException("Cannot evaluate variable expression. " +
                    "Insert value first");
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper expression.");
        }
    }

    /**
     * Class for storing a multi-exponentiation.
     */
    protected static class MultiExpContext {

        private List<GroupElement> bases;
        private List<BigInteger> exponents;

        MultiExpContext() {
            bases = new ArrayList<>();
            exponents = new ArrayList<>();
        }

        void addExponentiation(GroupElement base, BigInteger exponent, boolean inInversion) {
            // TODO: bases with exponents one need extra handling to avoid precomputations
            bases.add(base);
            if (inInversion) {
                exponents.add(BigInteger.ZERO.subtract(exponent).mod(base.getStructure().size()));
            } else {
                exponents.add(exponent);
            }

        }

        boolean allBasesSameGroup() {
            Group group = bases.get(0).getStructure();
            for (GroupElement base : bases) {
                if (base.getStructure() != group) {
                    return false;
                }
            }
            return true;
        }

        public List<GroupElement> getBases() {
            return bases;
        }

        public List<BigInteger> getExponents() {
            return exponents;
        }

        public String toString() {
            return "Bases: " + Arrays.toString(bases.toArray()) + "\n" +
                    "Exponents: " + Arrays.toString(exponents.toArray());
        }
    }

    @Override
    public GroupElementExpression optimize(GroupElementExpression expr) {
        return expr;
    }

    @Override
    public GroupElementExpression precompute(GroupElementExpression expr) {
        return expr;
    }

    @Override
    public BooleanExpression precompute(BooleanExpression expr) {
        return expr;

    }

    public void setEnableCachingInterleavedSliding(boolean newSetting) {
        enableCachingInterleavedSliding = newSetting;
    }

    public void setEnableCachingSimultaneous(boolean newSetting) {
        enableCachingSimultaneous = newSetting;
    }

    public void setEnableCachingInterleavedWnaf(boolean newSetting) {
        enableCachingInterleavedWnaf = newSetting;
    }

    /**
     * Enable/Disable caching for specified algorithm.
     * @param alg The algorithm to enable/disable caching for.
     * @param newSetting Whether caching should be enabled or disabled.
     */
    public void setEnableCachingForAlg(ForceMultiExpAlgorithmSetting alg, boolean newSetting) {
        switch (alg) {
            case INTERLEAVED_SLIDING:
                setEnableCachingInterleavedSliding(newSetting);
                return;
            case INTERLEAVED_WNAF:
                setEnableCachingInterleavedWnaf(newSetting);
                return;
            case SIMULTANEOUS:
                setEnableCachingSimultaneous(newSetting);
                return;
        }
        throw new IllegalArgumentException("Unsupported ForceMultiExpAlgorithmSetting value.");
    }

    public void setForcedMultiExpAlgorithm(ForceMultiExpAlgorithmSetting newSetting) {
        forcedMultiExpAlgorithm = newSetting;
    }

    public void setWindowSizeInterleavedSlidingCaching(int newSetting) {
        windowSizeInterleavedSlidingCaching = newSetting;
    }

    public void setWindowSizeInterleavedSlidingNoCaching(int newSetting) {
        this.windowSizeInterleavedSlidingNoCaching = newSetting;
    }

    public void setWindowSizeSimultaneousCaching(int newSetting) {
        windowSizeSimultaneousCaching = newSetting;
    }

    public void setWindowSizeSimultaneousNoCaching(int newSetting) {
        windowSizeSimultaneousNoCaching = newSetting;
    }

    /**
     * Upper bound for number of bases below which the simultaneous multi-exponentiation
     * approach may be used. Increasing this value too much can lead to full heap memory errors if
     * the precomputed power products take up too much space. Caching should then also be enabled
     * for the simultaneous algorithm to avoid the expensive repeated precomputation of
     * power products.
     * @param newSetting New upper bound for simultaneous algorithm usage.
     */
    public void setSimultaneousNumBasesCutoff(int newSetting) {
        this.simultaneousNumBasesCutoff = newSetting;
    }

    /**
     * Upper bound for cost of inversion in the group below the evaluator should use a WNAF-bases
     * multi-exponentiation algorithm. A value of 100 means that if 100 inversions cost as much or
     * less than 100 group operations, then the WNAF-based algorithm will be used.
     * @param newSetting New upper bound for WNAF usage.
     */
    public void setUseWnafCostInversion(int newSetting) {
        this.useWnafCostInversion = newSetting;
    }

    public enum ForceMultiExpAlgorithmSetting {
        DISABLED, INTERLEAVED_SLIDING, INTERLEAVED_WNAF, SIMULTANEOUS
    }
}
