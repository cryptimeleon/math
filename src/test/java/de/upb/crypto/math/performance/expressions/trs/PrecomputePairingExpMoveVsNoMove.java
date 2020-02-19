package de.upb.crypto.math.performance.expressions.trs;

import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.evaluator.NaiveGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.performance.expressions.ExpressionGenerator;

/**
 * For testing e(g_1^x, g_2) vs e(g_1, g_2)^x. In the latter case, e(g_1, g_2) can be pre-evaluated.
 * Result: With the slow pairing in this library,
 *  not moving any variables inside the pairing is better such that the pairing itself can be pre-evaluated.
 *
 *  The PairingGtExpRule has been adapted so this test does not work anymore as both cases behave similarly.
 *
 * @author Raphael Heitjohann
 */
public class PrecomputePairingExpMoveVsNoMove {

    public static void main(String[] args) {
        BilinearGroupFactory fac = new BilinearGroupFactory(80);
        fac.setDebugMode(false);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup bilGroup = fac.createBilinearGroup();

        int numPairings = 4;
        GroupElementExpression expr = ExpressionGenerator.genPairingWithMultiExpOutside(bilGroup.getBilinearMap(),
                numPairings, true);

        ValueBundle valueBundle = new ValueBundle();
        for (int i = 0; i < numPairings; ++i) {
            valueBundle.put("x" + i, bilGroup.getGT().getZn().getUniformlyRandomElement());
        }

        long startTime = System.currentTimeMillis();
        GroupElementExpression precomputedWithMove =  new OptGroupElementExpressionEvaluator().precompute(expr);
        System.out.println("Time of precomputation with move in ms: " + (System.currentTimeMillis() - startTime));

        OptGroupElementExpressionEvaluator noRewritingEvaluator = new OptGroupElementExpressionEvaluator();
        noRewritingEvaluator.getConfig().setEnablePrecomputeRewriting(false);
        startTime = System.currentTimeMillis();
        GroupElementExpression precomputedWithoutMove = noRewritingEvaluator.precompute(expr);
        System.out.println("Time of precomputation without move in ms: " + (System.currentTimeMillis() - startTime));

        GroupElement rightResult = expr.substitute(valueBundle)
                .evaluate(new NaiveGroupElementExpressionEvaluator());

        int numWarmupRuns = 2;
        int numRuns = 5;
        GroupElement tmp;

        System.out.println("----- Start runs with move: -----");

        for (int i = 0; i < numWarmupRuns + numRuns; ++i) {
            if (i < numWarmupRuns) {
                tmp = precomputedWithMove.substitute(valueBundle)
                        .evaluate(new OptGroupElementExpressionEvaluator());
                assert tmp.equals(rightResult);
            } else {
                startTime = System.currentTimeMillis();
                tmp = precomputedWithMove.substitute(valueBundle)
                        .evaluate(new OptGroupElementExpressionEvaluator());
                assert tmp.equals(rightResult);
                System.out.println("Time (ms): " + (System.currentTimeMillis() - startTime));
            }
        }

        System.out.println("----- Start runs without move: -----");

        for (int i = 0; i < numWarmupRuns + numRuns; ++i) {
            if (i < numWarmupRuns) {
                tmp = precomputedWithoutMove.substitute(valueBundle)
                        .evaluate(new OptGroupElementExpressionEvaluator());
                assert tmp.equals(rightResult);
            } else {
                startTime = System.currentTimeMillis();
                tmp = precomputedWithoutMove.substitute(valueBundle)
                        .evaluate(new OptGroupElementExpressionEvaluator());
                assert tmp.equals(rightResult);
                System.out.println("Time (ms): " + (System.currentTimeMillis() - startTime));
            }
        }
    }
}
