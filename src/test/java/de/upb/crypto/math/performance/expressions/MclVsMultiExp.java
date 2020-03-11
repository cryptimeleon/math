package de.upb.crypto.math.expressions.test;

import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluatorConfig;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory;
import de.upb.crypto.math.pairings.mcl.MclGroupT;
import de.upb.crypto.math.performance.expressions.ExpressionGenerator;

/**
 * Benchmark for comparing Mcl naive evaluation vs Java multi-exponentiation via the evaluator.
 */
public class MclVsMultiExp {
    public static void main(String[] args) {
        int[] baseNums = new int[] {5, 5, 10, 30, 30, 50};
        int[] expNums = new int[] {2, 5, 10, 15, 30, 50};
        Group group = new MclGroupT();
        for (int i = 0; i < baseNums.length; ++i) {
            GroupPrecomputationsFactory.get(group).reset();
            GroupElementExpression expr = ExpressionGenerator
                    .genMultiExponentiation(new MclGroupT(), baseNums[i], expNums[i]);
            System.out.println("Benchmarking with " + baseNums[i] + " bases and " + expNums[i] + " exponents:");
            benchmarkMcl(expr);
            benchmarkMultiExp(expr);
        }
    }

    public static void benchmarkMcl(GroupElementExpression expr) {
        long startTime = System.currentTimeMillis();
        expr.evaluateNaive();
        System.out.println("Mcl time(ms): " + (System.currentTimeMillis() - startTime));
    }

    public static void benchmarkMultiExp(GroupElementExpression expr) {
        long startTime = System.currentTimeMillis();
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_SLIDING
        );
        evaluator.getConfig().setEnableCachingAllAlgs(false);
        expr.evaluate(evaluator);
        System.out.println("MultiExp time(ms): " + (System.currentTimeMillis() - startTime));
    }
}
