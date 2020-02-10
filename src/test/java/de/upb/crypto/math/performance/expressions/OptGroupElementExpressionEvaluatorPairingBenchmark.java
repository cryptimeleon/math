package de.upb.crypto.math.performance.expressions;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;

public class OptGroupElementExpressionEvaluatorPairingBenchmark {

    public static void main(String[] args) {
        long startTime;
        long endTime;

        long avgTotalTime = 0;

        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup bilGroup = fac.createBilinearGroup();
        GroupElementExpression pairingExpr = ExpressionGenerator.genPairingWithMultiExp(
                bilGroup.getBilinearMap(),
                200, 200,
                200, 200
        );
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        // set this to switch multithreading option
        evaluator.getConfig().setEnableMultithreadedPairingEvaluation(false);

        int runs = 20;
        int warmupRuns = 4;
        for (int i = 0; i < runs; ++i) {
            startTime = System.currentTimeMillis();

            evaluator.evaluate(pairingExpr);

            endTime = System.currentTimeMillis();
            if (i >= warmupRuns) {
                System.out.println("------------------------------------------------------------");
                System.out.println("TOTAL TIME: " + (endTime - startTime));
                avgTotalTime += (endTime - startTime);
                System.out.println("------------------------------------------------------------");
            }
        }
        avgTotalTime /= runs - warmupRuns;
        System.out.println("AVERAGE TOTAL TIME: " + avgTotalTime);
    }
}
