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
 * @author Raphael Heitjohann
 */
public class OptGroupElementExpressionEvaluator implements GroupElementExpressionEvaluator {

    private boolean enableCachingInterleaved;
    private boolean enableCachingSimultaneous;
    private ForceMultiExpAlgorithmSetting forcedMultiExpAlgorithm;
    private int windowSizeInterleavedCaching;
    private int windowSizeInterleavedNoCaching;
    private int windowSizeSimultaneousCaching;
    private int windowSizeSimultaneousNoCaching;
    private int simultaneousNumBasesCutoff;

    public OptGroupElementExpressionEvaluator() {
        // TODO: best default values here?
        enableCachingInterleaved = true;
        enableCachingSimultaneous = true;
        forcedMultiExpAlgorithm = ForceMultiExpAlgorithmSetting.DISABLED;
        windowSizeInterleavedCaching = 8;
        windowSizeInterleavedNoCaching = 4;
        windowSizeSimultaneousCaching = 1;
        windowSizeSimultaneousNoCaching = 1;
        simultaneousNumBasesCutoff = 10;
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

    private GroupElement evaluateMultiExp(MultiExpContext multiExpContext) {
        // use swantes recommendations
        if ((multiExpContext.getBases().size() < simultaneousNumBasesCutoff
                || forcedMultiExpAlgorithm == ForceMultiExpAlgorithmSetting.SIMULTANEOUS)
                && forcedMultiExpAlgorithm != ForceMultiExpAlgorithmSetting.INTERLEAVED) {
            // either 1 or 2 window size
            if (enableCachingSimultaneous) {
                return simultaneousSlidingWindowMulExp(
                        multiExpContext,
                        windowSizeSimultaneousCaching
                );
            } else {
                return simultaneousSlidingWindowMulExp(
                        multiExpContext,
                        windowSizeSimultaneousNoCaching
                );
            }

        } else {
            if (enableCachingInterleaved) {
                return interleavingSlidingWindowMultiExp(
                        multiExpContext,
                        windowSizeInterleavedCaching
                );
            } else {
                return interleavingSlidingWindowMultiExp(
                        multiExpContext,
                        windowSizeInterleavedNoCaching
                );
            }
        }
    }

    private List<GroupElement> computePowerProducts(List<GroupElement> bases, int windowSize) {
        // TODO: Having this in two places (here and GroupPrecomputations) probably not ideal
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

    private GroupElement simultaneousSlidingWindowMulExp(MultiExpContext multiExpContext,
                                                         int windowSize) {
        // TODO: we should not do any precomputations for bases with exponents 1
        List<GroupElement> bases = multiExpContext.getBases();
        List<BigInteger> exponents = multiExpContext.getExponents();
        List<GroupElement> powerProducts = new ArrayList<>();
        if (enableCachingSimultaneous) {
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

    private GroupElement interleavingSlidingWindowMultiExp(MultiExpContext multiExpContext,
                                                           int windowSize) {
        List<GroupElement> bases = multiExpContext.getBases();
        List<BigInteger> exponents = multiExpContext.getExponents();
        List<List<GroupElement>> oddPowers = new ArrayList<>();
        if (enableCachingInterleaved) {
            GroupPrecomputationsFactory.GroupPrecomputations groupPrecomputations =
                    GroupPrecomputationsFactory.get(bases.get(0).getStructure());
            for (GroupElement base : bases) {
                oddPowers.add(groupPrecomputations.getOddPowers(base, (1 << windowSize) - 1));
            }
        } else {
            for (GroupElement base : bases) {
                oddPowers.add(UncachedGroupPrecomputations
                        .precomputeSmallOddPowers(base, (1 << windowSize) - 1));
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

    public void setEnableCachingInterleaved(boolean newSetting) {
        enableCachingInterleaved = newSetting;
    }

    public void setEnableCachingSimultaneous(boolean newSetting) {
        enableCachingSimultaneous = newSetting;
    }

    public void setForcedMultiExpAlgorithm(ForceMultiExpAlgorithmSetting newSetting) {
        forcedMultiExpAlgorithm = newSetting;
    }

    public void setWindowSizeInterleavedCaching(int newSetting) {
        windowSizeInterleavedCaching = newSetting;
    }

    public void setWindowSizeInterleavedNoCaching(int newSetting) {
        this.windowSizeInterleavedNoCaching = newSetting;
    }

    public void setWindowSizeSimultaneousCaching(int newSetting) {
        windowSizeSimultaneousCaching = newSetting;
    }

    public void setWindowSizeSimultaneousNoCaching(int newSetting) {
        windowSizeSimultaneousNoCaching = newSetting;
    }

    public void setSimultaneousNumBasesCutoff(int newSetting) {
        this.simultaneousNumBasesCutoff = newSetting;
    }

    public enum ForceMultiExpAlgorithmSetting {
        DISABLED, INTERLEAVED, SIMULTANEOUS
    }
}
