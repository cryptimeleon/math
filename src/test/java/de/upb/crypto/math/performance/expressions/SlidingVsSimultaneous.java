package de.upb.crypto.math.performance.expressions;

import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluatorConfig;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory;
import de.upb.crypto.math.pairings.mcl.MclGroup1;

public class SlidingVsSimultaneous {
    public static void main(String[] args) {
        int[] baseNums = new int[] {2, 5, 10};
        int[] expNums = new int[] {2, 5, 10};
        int numRuns = 40;
        int numWarmups = 5;
        Group group = new MclGroup1();
        GroupElementExpression[] exprs = new GroupElementExpression[50];
        long[][] mclTimes = new long[baseNums.length][numRuns];
        long[][] multiExpAutoTimes = new long[baseNums.length][numRuns];
        long[][] multiExpSlidingTimes = new long[baseNums.length][numRuns];
        for (int k = 0; k < numRuns + numWarmups; ++k) {
            System.out.println("----- Benchmark " + k + " -----");
            for (int i = 0; i < baseNums.length; ++i) {
                for (int j = 0; j < exprs.length; ++j) {
                    exprs[j] = ExpressionGenerator.genMultiExponentiation(group, baseNums[i], expNums[i]);
                }

                //System.out.println("Benchmarking with " + baseNums[i] + " bases and " + expNums[i] + " exponents:");
                long mclTime = benchmarkMcl(exprs);
                GroupPrecomputationsFactory.get(group).reset();
                long multiExpAutoTime = benchmarkMultiExpPrecompAuto(exprs);
                GroupPrecomputationsFactory.get(group).reset();
                long multiExpSlidingTime = benchmarkMultiExpPrecompSliding(exprs);
                if (k >= numWarmups) {
                    mclTimes[i][k - numWarmups] = mclTime;
                    multiExpAutoTimes[i][k - numWarmups] = multiExpAutoTime;
                    multiExpSlidingTimes[i][k - numWarmups] = multiExpSlidingTime;
                }
            }
        }
        for (int i = 0; i < baseNums.length; ++i) {
            System.out.println("----- Results for numBases " + baseNums[i] + ": -----");
            System.out.println("Mcl avg: " + average(mclTimes[i]));
            System.out.println("MultiExp (precomp, auto) avg: " + average(multiExpAutoTimes[i]));
            System.out.println("MultiExp (precomp, sliding) avg: " + average(multiExpSlidingTimes[i]));

            System.out.println("Mcl min: " + minimum(mclTimes[i]));
            System.out.println("MultiExp (precomp, auto) min: " + minimum(multiExpAutoTimes[i]));
            System.out.println("MultiExp (precomp, sliding) min: " + minimum(multiExpSlidingTimes[i]));

            System.out.println("Mcl stddev: " + standardDeviation(mclTimes[i]));
            System.out.println("MultiExp (precomp, auto) stddev: " + standardDeviation(multiExpAutoTimes[i]));
            System.out.println("MultiExp (precomp, sliding) stddev: " + standardDeviation(multiExpSlidingTimes[i]));
        }

    }

    public static double average(long[] dataPoints) {
        double avg = 0;
        for (long dataPoint : dataPoints) {
            avg += dataPoint;
        }
        return avg / dataPoints.length;
    }

    public static long minimum(long[] dataPoints) {
        long min = dataPoints[0];
        for (long dataPoint : dataPoints) {
            if (dataPoint < min)
                min = dataPoint;
        }
        return min;
    }

    public static double standardDeviation(long[] dataPoints) {
        double avg = average(dataPoints);
        double sum = 0;
        for (long dataPoint : dataPoints) {
            sum += Math.pow(dataPoint - avg, 2);
        }
        sum /= dataPoints.length - 1;
        return Math.sqrt(sum);
    }


    public static long benchmarkMcl(GroupElementExpression[] exprs) {
        long startTime = System.currentTimeMillis();
        for (GroupElementExpression expr : exprs)
            expr.evaluateNaive();
        long endTime = System.currentTimeMillis();
        //System.out.println("Mcl time(ms): " + (endTime - startTime));
        return endTime - startTime;
    }


    public static long benchmarkMultiExpPrecompAuto(GroupElementExpression[] exprs) {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setEnableCachingAllAlgs(true);
        evaluator.getConfig().setEnablePrecomputeRewriting(false);
        evaluator.getConfig().setEnablePrecomputeEvaluation(false);
        evaluator.getConfig().setWindowSizeSimultaneousCaching(1);
        evaluator.getConfig().setSimultaneousNumBasesCutoff(11);
        for (GroupElementExpression expr: exprs) {
            evaluator.precompute(expr);
        }
        long startTime = System.currentTimeMillis();
        for (GroupElementExpression expr : exprs)
            expr.evaluate(evaluator);
        long endTime = System.currentTimeMillis();
        //System.out.println("MultiExp (with precomp, auto) time(ms): " + (endTime - startTime));
        return endTime - startTime;
    }

    public static long benchmarkMultiExpPrecompSliding(GroupElementExpression[] exprs) {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_SLIDING
        );
        evaluator.getConfig().setEnableCachingAllAlgs(true);
        evaluator.getConfig().setEnablePrecomputeRewriting(false);
        evaluator.getConfig().setEnablePrecomputeEvaluation(false);
        for (GroupElementExpression expr: exprs) {
            evaluator.precompute(expr);
        }
        long startTime = System.currentTimeMillis();
        for (GroupElementExpression expr : exprs)
            expr.evaluate(evaluator);
        long endTime = System.currentTimeMillis();
        //System.out.println("MultiExp (with precomp, sliding) time(ms): " + (endTime - startTime));
        return endTime - startTime;
    }
}
