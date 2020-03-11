package de.upb.crypto.math.performance.expressions;

import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluatorConfig;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory;
import de.upb.crypto.math.pairings.mcl.MclGroup1;
import de.upb.crypto.math.pairings.mcl.MclGroupT;

/**
 * Benchmark for comparing Mcl naive evaluation vs Java multi-exponentiation via the evaluator.
 */
public class MclVsMultiExp {
    public static void main(String[] args) {
        int[] baseNums = new int[] {2, 5, 10, 30, 50, 100};
        int[] expNums = new int[] {2, 5, 10, 30, 50, 100};
        Group group = new MclGroup1();
        GroupElementExpression[] exprs = new GroupElementExpression[50];
        for (int i = 0; i < baseNums.length; ++i) {
            for (int j = 0; j < exprs.length; ++j) {
                exprs[j] = ExpressionGenerator.genMultiExponentiation(group, baseNums[i], expNums[i]);
            }

            System.out.println("Benchmarking with " + baseNums[i] + " bases and " + expNums[i] + " exponents:");
            benchmarkMcl(exprs);
            GroupPrecomputationsFactory.get(group).reset();
            benchmarkMultiExp(exprs);
            GroupPrecomputationsFactory.get(group).reset();
            benchmarkMultiExp2(exprs);
            GroupPrecomputationsFactory.get(group).reset();
            benchmarkMultiExpPrecomp(exprs);
        }
    }

    public static void benchmarkMcl(GroupElementExpression[] exprs) {
        long startTime = System.currentTimeMillis();
        for (GroupElementExpression expr : exprs)
            expr.evaluateNaive();
        System.out.println("Mcl time(ms): " + (System.currentTimeMillis() - startTime));
    }

    public static void benchmarkMultiExp(GroupElementExpression[] exprs) {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_SLIDING
        );
        evaluator.getConfig().setEnableCachingAllAlgs(false);
        long startTime = System.currentTimeMillis();
        for (GroupElementExpression expr : exprs)
            expr.evaluate(evaluator);
        System.out.println("MultiExp (int. sliding) time(ms): " + (System.currentTimeMillis() - startTime));
    }

    public static void benchmarkMultiExp2(GroupElementExpression[] exprs) {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_WNAF
        );
        evaluator.getConfig().setEnableCachingAllAlgs(false);
        long startTime = System.currentTimeMillis();
        for (GroupElementExpression expr : exprs)
            expr.evaluate(evaluator);
        System.out.println("MultiExp (int. wnaf) time(ms): " + (System.currentTimeMillis() - startTime));
    }

    public static void benchmarkMultiExpPrecomp(GroupElementExpression[] exprs) {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        /*evaluator.getConfig().setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_WNAF
        );*/
        evaluator.getConfig().setEnableCachingAllAlgs(true);
        evaluator.getConfig().setEnablePrecomputeRewriting(false);
        evaluator.getConfig().setEnablePrecomputeEvaluation(false);
        for (GroupElementExpression expr: exprs) {
            evaluator.precompute(expr);
        }
        long startTime = System.currentTimeMillis();
        for (GroupElementExpression expr : exprs)
            expr.evaluate(evaluator);
        System.out.println("MultiExp (with precomp) time(ms): " + (System.currentTimeMillis() - startTime));
    }
}
