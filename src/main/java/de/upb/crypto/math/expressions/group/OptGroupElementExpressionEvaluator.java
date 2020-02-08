package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.exponent.*;
import de.upb.crypto.math.interfaces.structures.*;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
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
    private boolean enableMultithreadedPairingEvaluation;
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

    private boolean enablePrecomputeEvaluation;
    private boolean enablePrecomputeCaching;

    private ThreadPoolExecutor pairingExecutor;

    public OptGroupElementExpressionEvaluator() {
        // TODO: best default values here? Could use even more finetuning
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

        enablePrecomputeCaching = true;
        enablePrecomputeEvaluation = true;

        // For parallel evaluation of both sides of a pariring
        // In the case of expensive pairings such as the BN pairing, this
        // seems to really only be worth it if the multi-exponentiation is very big,
        // and even then, the difference is not really noticable at all since the pairing
        // is so expensive.
        pairingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        enableMultithreadedPairingEvaluation = true;
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

        if (multiExpContext.isEmpty()) {
            return expr.getGroup().getNeutralElement();
        }
        // This is to remove any constants (exponent 1) from the multi-exponentiation.
        // (Atleast the ones on the left and on the right, we cannot remove the ones
        // in the middle unless we got commutativity.
        // We don't want to do any precomputations for those.
        GroupElement leftConstantsResult = multiExpContext.evalAndRemoveLeftConstants();
        GroupElement rightConstantsResult = expr.getGroup().getNeutralElement();
        if (!multiExpContext.isEmpty()) {
            rightConstantsResult = multiExpContext.evalAndRemoveRightConstants();
        }
        if (multiExpContext.isEmpty()) {
            return leftConstantsResult.op(rightConstantsResult);
        }
        return leftConstantsResult.op(evaluateMultiExp(multiExpContext)).op(rightConstantsResult);
    }

    /**
     * Evaluate multi-exponentiation using configured/default algorithms.
     * @param multiExpContext The multiexponentiation to evaluate.
     * @return Result of evaluation of multi-exponentiation.
     */
    private GroupElement evaluateMultiExp(MultiExpContext multiExpContext) {
        switch (getCurrentlyChosenMultiExpALgorithm(multiExpContext.getBases().size(),
                multiExpContext.getBases().get(0).getStructure().estimateCostOfInvert())) {
            case SIMULTANEOUS:
                return simultaneousSlidingWindowMulExpWrapper(multiExpContext);
            case INTERLEAVED_SLIDING:
                return interleavingSlidingWindowMultiExpWrapper(multiExpContext);
            case INTERLEAVED_WNAF:
                return interleavingWnafWindowMultiExpWrapper(multiExpContext);
        }
        throw new IllegalArgumentException("Unsupported MultiExpAlgorithm value.");
    }

    /**
     * Calculates which multi-exponentiation algorithm to use according to chosen settings.
     * @param numBases Number of bases in multi-exponentiation to evaluate.
     * @param costInversion Cost of inversion in group of multi-exponentiation.
     * @return The multi-exponentiation algorithm that would be chosen.
     */
    private MultiExpAlgorithm getCurrentlyChosenMultiExpALgorithm(int numBases, int costInversion) {
        switch (forcedMultiExpAlgorithm) {
            case SIMULTANEOUS:
                return MultiExpAlgorithm.SIMULTANEOUS;
            case INTERLEAVED_SLIDING:
                return MultiExpAlgorithm.INTERLEAVED_SLIDING;
            case INTERLEAVED_WNAF:
                return MultiExpAlgorithm.INTERLEAVED_WNAF;
            case DISABLED:
                // select algorithm based on swante scholz's recommendations
                if (numBases < simultaneousNumBasesCutoff
                        && enableCachingSimultaneous) {
                    return MultiExpAlgorithm.SIMULTANEOUS;
                } else if (costInversion <= useWnafCostInversion) {
                    return MultiExpAlgorithm.INTERLEAVED_WNAF;
                } else {
                    return MultiExpAlgorithm.INTERLEAVED_SLIDING;
                }
        }
        throw new IllegalArgumentException("Unsupported forcedMultiExpAlgorithm value.");
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
                    pow_expr.getBase().evaluateNaive(),
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
            final GroupElement[] lhs = new GroupElement[1];
            final GroupElement[] rhs = new GroupElement[1];
            if (enableMultithreadedPairingEvaluation) {
                GroupElementExpressionEvaluator thisEval = this;
                Future<Object> leftResult = pairingExecutor.submit(
                        () -> lhs[0] = pair_expr.getLhs().evaluate(thisEval));
                Future<Object> rightResult = pairingExecutor.submit(
                        () -> rhs[0] = pair_expr.getRhs().evaluate(thisEval));
                while (!leftResult.isDone() || !rightResult.isDone()) {}
            } else {
                lhs[0] = pair_expr.getLhs().evaluate(this);
                rhs[0] = pair_expr.getRhs().evaluate(this);
            }
            GroupElement pair_result = pair_expr.getMap().apply(lhs[0], rhs[0]);
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
            if (exponent.compareTo(BigInteger.ZERO) == 0) {
                return;
            }
            BigInteger realExponent;
            if(inInversion) {
                realExponent = exponent.negate();
            } else {
                realExponent = exponent;
            }
            // Move negative exponent into basis if possible
            if (realExponent.compareTo(BigInteger.ZERO) < 0) {
                exponents.add(realExponent.negate());
                bases.add(base.inv());
            } else {
                exponents.add(realExponent);
                bases.add(base);
            }

        }

        boolean allBasesSameGroup() {
            if (isEmpty()) {
                return true;
            }
            Group group = bases.get(0).getStructure();
            for (GroupElement base : bases) {
                if (base.getStructure() != group) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Avoid precomputing elements whose exponent is one (so no exponentiation necessary)
         * by evaluating them and then removing them from the multi-exponentiation context.
         * @return
         */
        GroupElement evalAndRemoveLeftConstants() {
            // assume size of bases not 0
            int numLeftConstants = 0;
            while (numLeftConstants < exponents.size()
                    && exponents.get(numLeftConstants).compareTo(BigInteger.ONE) == 0) {
                ++numLeftConstants;
            }
            GroupElement result = bases.get(0).getStructure().getNeutralElement();
            for (int i = 0; i < numLeftConstants; ++i) {
                result = result.op(bases.get(i));
            }
            // remove evaluated elements
            bases.subList(0, numLeftConstants).clear();
            exponents.subList(0, numLeftConstants).clear();
            return result;
        }

        /**
         * Same as for left but for right instead.
         * @return
         */
        GroupElement evalAndRemoveRightConstants() {
            // assume size of bases not 0
            int numRightConstants = 0;
            while (numRightConstants < exponents.size()
                    && exponents
                    .get(exponents.size()-1-numRightConstants).compareTo(BigInteger.ONE) == 0) {
                ++numRightConstants;
            }
            GroupElement result = bases.get(0).getStructure().getNeutralElement();
            for (int i = exponents.size()-numRightConstants; i < exponents.size(); ++i) {
                result = result.op(bases.get(i));
            }
            // remove evaluated elements
            bases.subList(exponents.size()-numRightConstants, exponents.size()).clear();
            exponents.subList(exponents.size()-numRightConstants, exponents.size()).clear();
            return result;
        }

        public boolean isEmpty() {
            return bases.size() == 0;
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
        GroupElementExpression newExpr = expr;
        if (enablePrecomputeEvaluation) {
            // Evaluate all expressions that do not contain variables to simplify expression as much as possible
            // For that we need to find expressions that do not contain variable first
            Map<GroupElementExpression, Boolean> exprToContainsVar = new HashMap<>();
            markExprWithVars(expr, exprToContainsVar);
            // Now we evaluate everything without variables and build a reduced new expression
            newExpr = evalWithoutVars(expr, exprToContainsVar);
        }
        if (enablePrecomputeCaching) {
            // Find bases contained in the expression such that precomputations can be done for them
            Map<GroupElementExpression, List<GroupElement>> exprToContBases = new HashMap<>();
            findContainedBases(newExpr, false, exprToContBases);
            cacheWindows(newExpr, exprToContBases);
        }
        return newExpr;
    }

    /**
     * Finds all bases contained in the expr and stores it as information.
     * @param expr Expression to find bases in.
     * @param exprToContBases Maps expressions to bases contained within them.
     */
    private void findContainedBases(GroupElementExpression expr, boolean inInversion,
                                    Map<GroupElementExpression, List<GroupElement>> exprToContBases) {
        // TODO: Would be nice to refactor this pattern,
        //  but recursion is required for inversion handling
        if (expr instanceof GroupOpExpr) {
            GroupOpExpr opExpr = (GroupOpExpr) expr;
            // group not necessarily commutative, so if we are in inversion, switch order
            if (inInversion) {
                findContainedBases(opExpr.getRhs(), inInversion, exprToContBases);
                findContainedBases(opExpr.getLhs(), inInversion, exprToContBases);
                List<GroupElement> containedBasesRight = exprToContBases.computeIfAbsent(opExpr.getRhs(),
                        k -> new LinkedList<>());
                List<GroupElement> containedBasesLeft = exprToContBases.computeIfAbsent(opExpr.getLhs(),
                        k -> new LinkedList<>());
                List<GroupElement> containedBases = exprToContBases.computeIfAbsent(expr, k -> new LinkedList<>());
                containedBases.addAll(containedBasesRight);
                containedBases.addAll(containedBasesLeft);
            } else {
                findContainedBases(opExpr.getLhs(), inInversion, exprToContBases);
                findContainedBases(opExpr.getRhs(), inInversion, exprToContBases);
                List<GroupElement> containedBasesLeft = exprToContBases.computeIfAbsent(opExpr.getLhs(),
                        k -> new LinkedList<>());
                List<GroupElement> containedBasesRight = exprToContBases.computeIfAbsent(opExpr.getRhs(),
                        k -> new LinkedList<>());
                List<GroupElement> containedBases = exprToContBases.computeIfAbsent(expr, k -> new LinkedList<>());
                containedBases.addAll(containedBasesLeft);
                containedBases.addAll(containedBasesRight);
            }
        } else if (expr instanceof GroupInvExpr) {
            GroupElementExpression invBase = ((GroupInvExpr) expr).getBase();
            findContainedBases(invBase, !inInversion, exprToContBases);
            // propagate found bases from base to inv expression
            List<GroupElement> invBaseContBases = exprToContBases.computeIfAbsent(invBase, k -> new LinkedList<>());
            List<GroupElement> invContBases = exprToContBases.computeIfAbsent(expr, k ->  new LinkedList<>());
            invContBases.addAll(invBaseContBases);
        } else if (expr instanceof GroupPowExpr) {
            GroupPowExpr powExpr = (GroupPowExpr) expr;
            // for now, just use evaluate naive on base and exponent
            List<GroupElement> containedBases = exprToContBases.computeIfAbsent(powExpr, k -> new LinkedList<>());
            // change this if we change multiexp algorithm to be smarter
            // if variable in there we cannot precompute easily
            containedBases.add(powExpr.base.evaluateNaive());
        } else if (expr instanceof GroupElementConstantExpr) {
            // count this as basis too for now since multiexp does it too
            List<GroupElement> containedBases = exprToContBases.computeIfAbsent(expr, k -> new LinkedList<>());
            containedBases.add(expr.evaluateNaive());
        } else if (expr instanceof GroupEmptyExpr) {
            // count this as basis too for now since multiexp does it too
            List<GroupElement> containedBases = exprToContBases.computeIfAbsent(expr, k -> new LinkedList<>());
            containedBases.add(expr.evaluateNaive());
        } else if (expr instanceof PairingExpr) {
            // Dont handle pairing here, need to call precompute on both sides but not here
        } else if (expr instanceof GroupVariableExpr) {
            // Dont handle variable here
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper expression.");
        }
    }

    private GroupElementExpression evalWithoutVars(GroupElementExpression expr,
                                                   Map<GroupElementExpression, Boolean> exprToContainsVar) {
        if (!exprToContainsVar.get(expr)) {
            return expr.evaluate().expr();
        } else {
            if (expr instanceof GroupOpExpr) {
                GroupOpExpr opExpr = (GroupOpExpr) expr;
                return new GroupOpExpr(
                        evalWithoutVars(opExpr.getLhs(), exprToContainsVar),
                        evalWithoutVars(opExpr.getRhs(), exprToContainsVar)
                );
            } else if (expr instanceof GroupInvExpr) {
                GroupInvExpr invExpr = (GroupInvExpr) expr;
                return new GroupInvExpr(
                        evalWithoutVars(invExpr.getBase(), exprToContainsVar)
                );
            } else if (expr instanceof GroupPowExpr) {
                GroupPowExpr powExpr = (GroupPowExpr) expr;
                // We don't go deeper into exponent
                return new GroupPowExpr(
                        evalWithoutVars(powExpr.getBase(), exprToContainsVar), powExpr.getExponent()
                );
            } else if (expr instanceof PairingExpr) {
                PairingExpr pairingExpr = (PairingExpr) expr;
                return new PairingExpr(
                        pairingExpr.getMap(),
                        evalWithoutVars(pairingExpr.getRhs(), exprToContainsVar),
                        evalWithoutVars(pairingExpr.getLhs(), exprToContainsVar)
                );
            } else if (expr instanceof GroupVariableExpr) {
                return expr;
            } else if (expr instanceof GroupElementConstantExpr || expr instanceof GroupEmptyExpr) {
                throw new IllegalStateException("Expression contains variable although it cannot.");
            } else {
                throw new IllegalArgumentException("Found something in expression tree that" +
                        "is not a proper group expression.");
            }
        }
    }

    /**
     * Traverses the given expression and marks call sub-expressions that contain a variable.
     * @param expr The expression to search.
     * @param exprToContainsVar Maps each sub-expression to a Boolean indicating whether the expression contains
     *                          a variable or not.
     */
    private void markExprWithVars(GroupElementExpression expr, Map<GroupElementExpression, Boolean> exprToContainsVar) {
        if (expr instanceof GroupOpExpr) {
            GroupOpExpr opExpr = (GroupOpExpr) expr;
            markExprWithVars(opExpr.getRhs(), exprToContainsVar);
            markExprWithVars(opExpr.getLhs(), exprToContainsVar);
            exprToContainsVar.put(
                    opExpr, exprToContainsVar.get(opExpr.getLhs()) || exprToContainsVar.get(opExpr.getRhs())
            );
        } else if (expr instanceof GroupInvExpr) {
            GroupInvExpr invExpr = (GroupInvExpr) expr;
            markExprWithVars(invExpr.getBase(), exprToContainsVar);
            exprToContainsVar.put(
                    invExpr, exprToContainsVar.get(invExpr.getBase())
            );
        } else if (expr instanceof GroupPowExpr) {
            GroupPowExpr powExpr = (GroupPowExpr) expr;
            markExprWithVars(powExpr.getBase(), exprToContainsVar);
            exprToContainsVar.put(
                    powExpr, exprToContainsVar.get(powExpr.getBase()) || containsVariableExpr(powExpr.getExponent())
            );
        } else if (expr instanceof GroupElementConstantExpr) {
            exprToContainsVar.put(expr, false);
        } else if (expr instanceof GroupEmptyExpr) {
            exprToContainsVar.put(expr, false);
        } else if (expr instanceof PairingExpr) {
            PairingExpr pairingExpr = (PairingExpr) expr;
            markExprWithVars(pairingExpr.getLhs(), exprToContainsVar);
            markExprWithVars(pairingExpr.getRhs(), exprToContainsVar);
            exprToContainsVar.put(
                    pairingExpr,
                    exprToContainsVar.get(pairingExpr.getLhs()) || exprToContainsVar.get(pairingExpr.getRhs())
            );
        } else if (expr instanceof GroupVariableExpr) {
            exprToContainsVar.put(expr, true);
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper expression.");
        }
    }

    /**
     * Checks whether expression contains a variable expression.
     * @param expr Expression to check.
     * @return true if expression contains a variable expression, else false.
     */
    private boolean containsVariableExpr(GroupElementExpression expr) {
        if (expr instanceof GroupOpExpr) {
            GroupOpExpr opExpr = (GroupOpExpr) expr;
            return containsVariableExpr(opExpr.getLhs())
                    || containsVariableExpr(opExpr.getRhs());
        } else if (expr instanceof GroupInvExpr) {
            GroupInvExpr invExpr = (GroupInvExpr) expr;
            return containsVariableExpr(invExpr.getBase());
        } else if (expr instanceof GroupPowExpr) {
            GroupPowExpr powExpr = (GroupPowExpr) expr;
            return containsVariableExpr(powExpr.getBase()) || containsVariableExpr(powExpr.getExponent());
        } else if (expr instanceof GroupElementConstantExpr) {
            return false;
        } else if (expr instanceof GroupEmptyExpr) {
            return false;
        } else if (expr instanceof PairingExpr) {
            PairingExpr pairingExpr = (PairingExpr) expr;
            return containsVariableExpr(pairingExpr.getLhs())
                    || containsVariableExpr(pairingExpr.getRhs());
        } else if (expr instanceof GroupVariableExpr) {
            return true;
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper group expression.");
        }
    }

    private boolean containsVariableExpr(ExponentExpr expr) {
        if (expr instanceof ExponentConstantExpr) {
            return false;
        } else if (expr instanceof ExponentEmptyExpr) {
            return false;
        } else if (expr instanceof ExponentInvExpr) {
            ExponentInvExpr invExpr = (ExponentInvExpr) expr;
            return containsVariableExpr(invExpr.getChild());
        } else if (expr instanceof ExponentMulExpr) {
            ExponentMulExpr mulExpr = (ExponentMulExpr) expr;
            return containsVariableExpr(mulExpr.getLhs()) || containsVariableExpr(mulExpr.getRhs());
        } else if (expr instanceof ExponentNegExpr) {
            ExponentNegExpr negExpr = (ExponentNegExpr) expr;
            return containsVariableExpr(negExpr.getChild());
        } else if (expr instanceof  ExponentSumExpr) {
            ExponentSumExpr sumExpr = (ExponentSumExpr) expr;
            return containsVariableExpr(sumExpr.getLhs()) || containsVariableExpr(sumExpr.getLhs());
        } else if (expr instanceof ExponentVariableExpr) {
            return true;
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper exponent expression.");
        }
    }

    /**
     * Precomputes and caches odd powers/power products for later multi-exponentiation. Which is
     * done depends on the algorithm that would be selected for evaluation with the current
     * settings.
     * @param expr Expression to cache powers for.
     * @param exprToContainedBases Maps expressions to bases contained within them.
     */
    private void cacheWindows(GroupElementExpression expr,
                              Map<GroupElementExpression, List<GroupElement>> exprToContainedBases) {
        // Find out which algorithm would be used with current settings and do precomputation
        // for that algorithm.
        List<GroupElement> rootBases = exprToContainedBases.get(expr);
        int rootNumBases = rootBases.size();
        int costOfInversion = rootBases.get(0).getStructure().estimateCostOfInvert();
        MultiExpAlgorithm alg = getCurrentlyChosenMultiExpALgorithm(rootNumBases, costOfInversion);
        // Precompute powers, we always use caching window size.
        switch (alg) {
            case INTERLEAVED_WNAF:
                // precompute odd powers
                int maxExp = (1 << windowSizeInterleavedWnafCaching) - 1;
                GroupPrecomputationsFactory.GroupPrecomputations groupPrecomputations =
                        GroupPrecomputationsFactory.get(rootBases.get(0).getStructure());
                for (GroupElement base : rootBases) {
                    groupPrecomputations.addOddPowers(base, maxExp);
                }
                return;
            case INTERLEAVED_SLIDING:
                // precompute odd powers
                maxExp = (1 << windowSizeInterleavedSlidingCaching) - 1;
                groupPrecomputations = GroupPrecomputationsFactory
                        .get(rootBases.get(0).getStructure());
                for (GroupElement base : rootBases) {
                    groupPrecomputations.addOddPowers(base, maxExp);
                }
                return;
            case SIMULTANEOUS:
                // precompute power products
                // power products depends on order of bases, so they should be left to right as
                // in multiexp finding alg (sorting does not work if commutativity not given)
                groupPrecomputations = GroupPrecomputationsFactory
                        .get(rootBases.iterator().next().getStructure());
                groupPrecomputations.addPowerProducts(rootBases, windowSizeSimultaneousCaching);
        }
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
     * Enable/Disable caching for specified algorithm. Can be useful if you know a base
     * will only be used once. Otherwise (especially for the simultaneous algorithm) caching
     * should be enabled.
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

    public void setEnableMultithreadedPairingEvaluation(boolean newSetting) {
        enableMultithreadedPairingEvaluation = newSetting;
    }

    /**
     * Allows forcing a specific multi-exponentiation algorithm to be used.
     * Inappropriate usage (such as forcing simultaneous without caching) can lead
     * to terrible performance.
     * @param newSetting Algorithm to force usage of.
     */
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


    public void setEnablePrecomputeEvaluation(boolean enablePrecomputeEvaluation) {
        this.enablePrecomputeEvaluation = enablePrecomputeEvaluation;
    }

    public void setEnablePrecomputeCaching(boolean enablePrecomputeCaching) {
        this.enablePrecomputeCaching = enablePrecomputeCaching;
    }

    public enum ForceMultiExpAlgorithmSetting {
        DISABLED, INTERLEAVED_SLIDING, INTERLEAVED_WNAF, SIMULTANEOUS
    }

    private enum MultiExpAlgorithm {
        INTERLEAVED_SLIDING, INTERLEAVED_WNAF, SIMULTANEOUS
    }


    public boolean isEnableCachingInterleavedSliding() {
        return enableCachingInterleavedSliding;
    }

    public boolean isEnableCachingSimultaneous() {
        return enableCachingSimultaneous;
    }

    public boolean isEnableCachingInterleavedWnaf() {
        return enableCachingInterleavedWnaf;
    }

    public boolean isEnableMultithreadedPairingEvaluation() {
        return enableMultithreadedPairingEvaluation;
    }

    public ForceMultiExpAlgorithmSetting getForcedMultiExpAlgorithm() {
        return forcedMultiExpAlgorithm;
    }

    public int getWindowSizeInterleavedSlidingCaching() {
        return windowSizeInterleavedSlidingCaching;
    }

    public int getWindowSizeInterleavedSlidingNoCaching() {
        return windowSizeInterleavedSlidingNoCaching;
    }

    public int getWindowSizeInterleavedWnafCaching() {
        return windowSizeInterleavedWnafCaching;
    }

    public int getWindowSizeInterleavedWnafNoCaching() {
        return windowSizeInterleavedWnafNoCaching;
    }

    public int getWindowSizeSimultaneousCaching() {
        return windowSizeSimultaneousCaching;
    }

    public int getWindowSizeSimultaneousNoCaching() {
        return windowSizeSimultaneousNoCaching;
    }

    public int getSimultaneousNumBasesCutoff() {
        return simultaneousNumBasesCutoff;
    }

    public int getUseWnafCostInversion() {
        return useWnafCostInversion;
    }

    public boolean isEnablePrecomputeEvaluation() {
        return enablePrecomputeEvaluation;
    }

    public boolean isEnablePrecomputeCaching() {
        return enablePrecomputeCaching;
    }
}
