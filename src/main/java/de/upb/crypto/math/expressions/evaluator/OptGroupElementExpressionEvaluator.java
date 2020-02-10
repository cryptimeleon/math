package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.group.*;
import de.upb.crypto.math.interfaces.structures.*;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static de.upb.crypto.math.expressions.evaluator.MultiExpAlgorithms.*;

/**
 * Class for optimized evaluation of expressions. Can recognize multi-exponentiations in
 * expression trees and evaluates them using some appropriate multi-exponentiation algorithm.
 *
 * For explanation of algorithms see Swante Scholz's master thesis.
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

    public OptGroupElementExpressionEvaluator(OptGroupElementExpressionEvaluatorConfig config) {
        this.config = config;
        this.precomputer = new OptGroupElementExpressionPrecomputer(this.config);
        this.pairingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
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

    private GroupElement simultaneousSlidingWindowMulExpWrapper(MultiExpContext multiExpContext) {
        if (config.isEnableCachingSimultaneous()) {
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

    private GroupElement interleavingSlidingWindowMultiExpWrapper(MultiExpContext multiExpContext) {
        if (config.isEnableCachingInterleavedSliding()) {
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

    private GroupElement interleavingWnafWindowMultiExpWrapper(MultiExpContext multiExpContext) {
        if (config.isEnableCachingInterleavedWnaf()) {
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

    @Override
    public GroupElementExpression optimize(GroupElementExpression expr) {
        return expr;
    }

    @Override
    public GroupElementExpression precompute(GroupElementExpression expr) {
        GroupElementExpression newExpr = expr;
        if (config.isEnablePrecomputeEvaluation()) {
            // Evaluate all expressions that do not contain variables to simplify expression as much as possible
            // For that we need to find expressions that do not contain variable first
            Map<GroupElementExpression, Boolean> exprToContainsVar = new HashMap<>();
            precomputer.markExprWithVars(expr, exprToContainsVar);
            // Now we evaluate everything without variables and build a reduced new expression
            newExpr = precomputer.evalWithoutVars(expr, exprToContainsVar);
        }
        if (config.isEnablePrecomputeCaching()) {
            // Find bases contained in the expression such that precomputations can be done for them
            Map<GroupElementExpression, List<GroupElement>> exprToContBases = new HashMap<>();
            precomputer.findContainedBases(newExpr, false, exprToContBases);
            precomputer.cacheWindows(newExpr, exprToContBases);
        }
        return newExpr;
    }


    @Override
    public BooleanExpression precompute(BooleanExpression expr) {
        return expr;

    }

    public OptGroupElementExpressionEvaluatorConfig getConfig() {
        return this.config;
    }
}
