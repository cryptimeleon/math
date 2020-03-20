package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.bool.*;
import de.upb.crypto.math.expressions.evaluator.trs.RuleApplicator;
import de.upb.crypto.math.expressions.group.*;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static de.upb.crypto.math.expressions.evaluator.MultiExpAlgorithms.*;

/**
 * Class for optimized evaluation of expressions. Can recognize multi-exponentiations in
 * expression trees and evaluates them using the appropriate multi-exponentiation algorithm.
 * Furthermore, pre-computation allows for different optimizations such as pre-computing and caching
 * powers for the used multi-exponentiation algorithm and rewriting of the expression to be more
 * efficiently evaluable later.
 *
 * Most optimizations can be configured using the {@link OptGroupElementExpressionEvaluatorConfig}
 * accessible through {@link OptGroupElementExpressionEvaluator#getConfig()}.
 *
 * @author Raphael Heitjohann
 */
public class OptGroupElementExpressionEvaluator implements GroupElementExpressionEvaluator {

    private OptGroupElementExpressionEvaluatorConfig config;
    private OptGroupElementExpressionPrecomputer precomputer;

    private ThreadPoolExecutor pairingExecutor;

    public OptGroupElementExpressionEvaluator() {
        this.config = new OptGroupElementExpressionEvaluatorConfig();
        this.precomputer = new OptGroupElementExpressionPrecomputer(this.config);
        this.pairingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    }

    /**
     * Initialize the evaluator with a given configuration.
     * @param config The configuration to use.
     */
    public OptGroupElementExpressionEvaluator(OptGroupElementExpressionEvaluatorConfig config) {
        this.config = config;
        this.precomputer = new OptGroupElementExpressionPrecomputer(this.config);
        this.pairingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    }

    /**
     * Evaluate the given expression. Extracts the multi-exponentiation, if available, and uses
     * the multi-exponentiation algorithm given by the configuration to evaluate it.
     */
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
        switch (config.getCurrentlyChosenMultiExpAlgorithm(multiExpContext.getBases().size(),
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
     * Decides whether to use the simultaneous sliding window multi-exponentiation algorithm with caching or
     * without depending on the configuration, and executes the corresponding algorithm.
     * @param multiExpContext The multi-exponentiation to evaluate.
     * @return The result of evaluating the given multi-exponentiation.
     */
    private GroupElement simultaneousSlidingWindowMulExpWrapper(MultiExpContext multiExpContext) {
        if (config.isEnableCachingSimultaneous() && config.isEnableCachingAllAlgs()) {
            return simultaneousSlidingWindowMulExp(
                    multiExpContext,
                    config.getWindowSizeSimultaneousCaching(),
                    true
            );
        } else {
            return simultaneousSlidingWindowMulExp(
                    multiExpContext,
                    config.getWindowSizeInterleavedSlidingNoCaching(),
                    false
            );
        }
    }

    /**
     * Decides whether to use the interleaving sliding window multi-exponentiation algorithm with caching or without
     * depending on the configuraation, and executes the corresponding algorithm.
     * @param multiExpContext The multi-exponentiation to evaluate.
     * @return The result of evaluating the given multi-exponentiation.
     */
    private GroupElement interleavingSlidingWindowMultiExpWrapper(MultiExpContext multiExpContext) {
        if (config.isEnableCachingInterleavedSliding() && config.isEnableCachingAllAlgs()) {
            return interleavingSlidingWindowMultiExp(
                    multiExpContext,
                    config.getWindowSizeInterleavedSlidingCaching(),
                    true
            );
        } else {
            return interleavingSlidingWindowMultiExp(
                    multiExpContext,
                    config.getWindowSizeInterleavedSlidingNoCaching(),
                    false
            );
        }
    }

    /**
     * Decides whether to use the interleaving WNAF window multi-exponentiation algorithm with caching or without
     * depending on the configuraation, and executes the corresponding algorithm.
     * @param multiExpContext The multi-exponentiation to evaluate.
     * @return The result of evaluating the given multi-exponentiation.
     */
    private GroupElement interleavingWnafWindowMultiExpWrapper(MultiExpContext multiExpContext) {
        if (config.isEnableCachingInterleavedWnaf() && config.isEnableCachingAllAlgs()) {
            return interleavingWnafMultiExp(
                    multiExpContext,
                    config.getWindowSizeInterleavedWnafCaching(),
                    true
            );
        } else {
            return interleavingWnafMultiExp(
                    multiExpContext,
                    config.getWindowSizeInterleavedWnafNoCaching(),
                    false
            );
        }
    }

    /**
     * Takes an expression tree and flattens it into a multi-exponentiation using a simple
     * depth-first approach.
     * @param expr Expression to extract multi-exponentiation from.
     * @param inInversion Whether we currently are in an uneven level of inversion.
     *                    This is important as inversion needs to be propagated to each base/exponent pair
     *                    that we extract from the expression.
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
            if (config.isEnableMultithreadedPairingEvaluation()) {
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
     * Pre-computes given expression to obtain a more efficiently evaluable expression as well as
     * pre-computes powers and caches them such that later multi-exponentiation can be done more efficiently.
     * What exactly is done can be configured using the configuration obtainable by
     * {@link OptGroupElementExpressionEvaluator#getConfig()}.
     *
     * @param expr The expression to pre-compute.
     * @return The pre-computed expression.
     */
    @Override
    public GroupElementExpression precompute(GroupElementExpression expr) {
        GroupElementExpression newExpr = expr;
        if (config.isEnablePrecomputeRewriting()) {
            // Rewrite the expression to be more efficiently evaluatable and to make some more pre-evaluations possible
            // note: we could also move rule applicator into config, then user can customize that.
            newExpr = (GroupElementExpression) precomputer.rewriteTerms(
                    newExpr, new RuleApplicator(this.config.getGroupRewritingRules())
            );
        }

        if (config.isEnablePrecomputeEvaluation()) {
            // Evaluate all expressions that do not contain variables to simplify expression as much as possible
            // For that we need to find expressions that do not contain variable first
            Map<GroupElementExpression, Boolean> exprToContainsVar = new HashMap<>();
            precomputer.markExprWithVars(newExpr, exprToContainsVar);
            // Now we evaluate everything without variables and build a reduced new expression
            newExpr = precomputer.evalWithoutVars(newExpr, exprToContainsVar);
        }
        if (config.isEnablePrecomputeCaching()) {
            // Find bases contained in the expression such that precomputations can be done for them
            Map<GroupElementExpression, List<GroupElement>> exprToContBases = new HashMap<>();
            precomputer.findContainedBases(newExpr, false, exprToContBases);
            precomputer.cacheWindows(newExpr, exprToContBases);
        }
        return newExpr;
    }


    /**
     * Performs the same pre-computations as
     * {@link OptGroupElementExpressionEvaluator#precompute(GroupElementExpression)} but additionally
     * performs boolean-specific optimizations such as rewriting {@link GroupEqualityExpr}, and, if enabled,
     * can merge contained {@link BoolAndExpr} into a multi-exponentiation.
     * @param expr an expression containing, among others, expressions about this group
     * @return The pre-computed expression.
     */
    @Override
    public BooleanExpression precompute(BooleanExpression expr) {
        BooleanExpression newExpr = expr;
        if (config.isEnablePrecomputeRewriting()) {
            newExpr = (BooleanExpression) precomputer.rewriteTerms(newExpr);
        }
        // Next, try to merge ANDs of GroupEqualityExprs into one MultiExp
        if (config.isEnablePrecomputeProbabilisticAndMerging()) {
            Map<Expression, Boolean> exprToMergeable = new HashMap<>();
            precomputer.markMergeableExprs(newExpr, exprToMergeable);
            newExpr = (BooleanExpression) precomputer.traverseMergeANDs(newExpr, exprToMergeable);
        }
        return (BooleanExpression) this.precomputeBoolRecursive(newExpr);
    }

    /**
     * Searches through boolean expression tree and executes
     * {@link OptGroupElementExpressionEvaluator#precompute(GroupElementExpression)} on the left and right side
     * of any found {@link GroupEqualityExpr}.
     * @param expr The expression to pre-compute.
     * @return Pre-computed expression.
     */
    private Expression precomputeBoolRecursive(BooleanExpression expr) {
        if (expr instanceof BoolAndExpr) {
            BoolAndExpr andExpr = (BoolAndExpr) expr;
            return new BoolAndExpr(
                    (BooleanExpression) this.precomputeBoolRecursive(andExpr.getLhs()),
                    (BooleanExpression) this.precomputeBoolRecursive(andExpr.getRhs())
            );
        } else if (expr instanceof BoolOrExpr) {
            BoolOrExpr orExpr = (BoolOrExpr) expr;
            return new BoolOrExpr(
                    (BooleanExpression) this.precomputeBoolRecursive(orExpr.getLhs()),
                    (BooleanExpression) this.precomputeBoolRecursive(orExpr.getRhs())
            );
        } else if (expr instanceof BoolNotExpr) {
            BoolNotExpr notExpr = (BoolNotExpr) expr;
            return new BoolNotExpr(
                    (BooleanExpression) this.precomputeBoolRecursive(notExpr.getChild())
            );
        } else if (expr instanceof BoolEmptyExpr || expr instanceof BoolVariableExpr
                || expr instanceof ExponentEqualityExpr) {
            return expr;
        } else if (expr instanceof GroupEqualityExpr) {
            GroupEqualityExpr equalityExpr = (GroupEqualityExpr) expr;
            return new GroupEqualityExpr(
                    this.precompute(equalityExpr.getLhs()),
                    this.precompute(equalityExpr.getRhs())
            );
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper boolean expression: " + expr.getClass());
        }
    }

    /**
     * Retrieve {@link OptGroupElementExpressionEvaluatorConfig} used to configure the evaluator. Allows
     * for changing the configuration via the setters of the object.
     * @return The configuration.
     */
    public OptGroupElementExpressionEvaluatorConfig getConfig() {
        return this.config;
    }
}
