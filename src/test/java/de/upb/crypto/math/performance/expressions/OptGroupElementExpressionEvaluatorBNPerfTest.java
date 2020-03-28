package de.upb.crypto.math.performance.expressions;

import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import de.upb.crypto.math.expressions.evaluator.NaiveGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluatorConfig;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Performance tests for multi-exponentiation algorithms using group 1 for a BN pairing.
 */
@RunWith(Parameterized.class)
public class OptGroupElementExpressionEvaluatorBNPerfTest {

    @Parameterized.Parameters(name= "{index}: algorithm={0}")
    public static Collection<OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting>
    algs() {
        return Arrays.asList(
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_SLIDING,
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_WNAF,
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.SIMULTANEOUS
        );
    }

    public OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting algSetting;

    public OptGroupElementExpressionEvaluatorBNPerfTest(OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting algSetting) {
        this.algSetting = algSetting;
    }

    @Rule
    public JUnitPerfRule perfTestRule = new JUnitPerfRule();
    private final int perfDuration = 10_000;
    private final int warmupDuration = 4_000;

    /**
     * Test data for expression with many different bases, so bad for simultaneous.
     */
    private static GroupElementExpression manyPerfTestExpr;
    private static GroupElement manyExprResult;

    /**
     * Test data for expression with fewer bases, so good for simultaneous.
     */
    private static GroupElementExpression fewPerfTestExpr;
    private static GroupElement fewExprResult;


    @BeforeClass
    public static void setupPerfTest() {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup bilGroup = fac.createBilinearGroup();
        manyPerfTestExpr = ExpressionGenerator
                .genMultiExponentiation(bilGroup.getG1(), 11, 11);
        fewPerfTestExpr = ExpressionGenerator
                .genMultiExponentiation(bilGroup.getG1(), 6, 6);
        // Do precomputation beforehand
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setEnablePrecomputeEvaluation(false);
        evaluator.getConfig().setEnablePrecomputeRewriting(false);
        evaluator.precompute(manyPerfTestExpr);
        evaluator.precompute(fewPerfTestExpr);
        NaiveGroupElementExpressionEvaluator naiveEval = new NaiveGroupElementExpressionEvaluator();
        manyExprResult = manyPerfTestExpr.evaluate(naiveEval);
        fewExprResult = fewPerfTestExpr.evaluate(naiveEval);
    }

    @Test
    public void testManyCorrectnessCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        assertEquals(manyExprResult, manyPerfTestExpr.evaluate(evaluator));
    }

    @Test
    public void testManyCorrectnessNoCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setEnableCachingForAlg(algSetting, false);
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        assertEquals(manyExprResult, manyPerfTestExpr.evaluate(evaluator));
    }

    @Test
    public void testFewCorrectnessCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        assertEquals(fewExprResult, fewPerfTestExpr.evaluate(evaluator));
    }

    @Test
    public void testFewCorrectnessNoCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setEnableCachingForAlg(algSetting, false);
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        assertEquals(fewExprResult, fewPerfTestExpr.evaluate(evaluator));
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testManyBasesOptCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        manyPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testManyBasesOptNoCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setEnableCachingForAlg(algSetting, false);
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        manyPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testFewBasesOptCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        fewPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testFewBasesOptNoCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setEnableCachingForAlg(algSetting, false);
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        fewPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testManyBasesNaivePerf() {
        NaiveGroupElementExpressionEvaluator naiveEval = new NaiveGroupElementExpressionEvaluator();
        manyPerfTestExpr.evaluate(naiveEval);
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testFewBasesNaivePerf() {
        NaiveGroupElementExpressionEvaluator naiveEval = new NaiveGroupElementExpressionEvaluator();
        fewPerfTestExpr.evaluate(naiveEval);
    }
}

