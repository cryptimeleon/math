package de.upb.crypto.math.performance.expressions;

import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.expressions.group.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory;
import de.upb.crypto.math.structures.zn.Zn;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Performance tests for multi-exponentiation algorithms using group 1 for a BN pairing.
 */
//@Ignore
@RunWith(Parameterized.class)
public class OptGroupElementExpressionEvaluatorBNPerfTest {

    @Parameterized.Parameters(name= "{index}: algorithm={0}")
    public static Iterable<OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting>
    algs() {
        return Arrays.asList(
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.INTERLEAVED_SLIDING,
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.INTERLEAVED_WNAF,
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.SIMULTANEOUS
        );
    }

    @Parameterized.Parameter
    public OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting algSetting;

    @Rule
    public JUnitPerfRule perfTestRule = new JUnitPerfRule();
    public final int perfDuration = 10_000;
    public final int warmupDuration = 4_000;

    static Zn exponentZn;

    /**
     * Test data for expression with many different bases, so bad for simultaneous
     */
    static GroupElement[] manyBases;
    static Zn.ZnElement[] manyExponents;
    static GroupElementExpression manyPerfTestExpr;
    static GroupElement manyExprResult;

    static GroupElement[] fewBases;
    static Zn.ZnElement[] fewExponents;
    static GroupElementExpression fewPerfTestExpr;
    static GroupElement fewExprResult;

    static BilinearGroup bilGroup;


    @BeforeClass
    public static void setupPerfTest() {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        bilGroup = fac.createBilinearGroup();
        exponentZn = bilGroup.getG1().getZn();
        int manyNumBases = 11;
        int manyNumExponents = 11;
        manyBases = new GroupElement[manyNumBases];
        for (int i = 0; i < manyBases.length; ++i) {
            manyBases[i] = bilGroup.getG1().getUniformlyRandomNonNeutral();
            // Do precomputations before
            GroupPrecomputationsFactory
                    .get(bilGroup.getG1())
                    .getOddPowers(manyBases[i], (1<<8)-1);
        }
        // Do precomputations before
        GroupPrecomputationsFactory
                .get(bilGroup.getG1())
                .getPowerProducts(Arrays.asList(manyBases), 1);
        manyExponents = new Zn.ZnElement[manyNumExponents];
        for (int i = 0; i < manyExponents.length; ++i) {
            manyExponents[i] = exponentZn.getUniformlyRandomElement();
        }
        manyPerfTestExpr = new GroupPowExpr(manyBases[0].expr(),
                manyExponents[0].asExponentExpression());
        for (int i = 1; i < manyExponents.length; ++i) {
            manyPerfTestExpr = manyPerfTestExpr
                    .opPow(manyBases[i % manyNumBases], manyExponents[i]);
        }
        manyExprResult = manyPerfTestExpr.evaluate();

        int fewNumBases = 6;
        int fewNumExponents = 6;
        fewBases = new GroupElement[fewNumBases];
        for (int i = 0; i < fewBases.length; ++i) {
            fewBases[i] = bilGroup.getG1().getUniformlyRandomNonNeutral();
            GroupPrecomputationsFactory
                    .get(bilGroup.getG1())
                    .getOddPowers(fewBases[i], (1<<8)-1);
        }
        // Do precomputations before
        GroupPrecomputationsFactory
                .get(bilGroup.getG1())
                .getPowerProducts(Arrays.asList(fewBases), 1);
        fewExponents = new Zn.ZnElement[fewNumExponents];
        for (int i = 0; i < fewExponents.length; ++i) {
            fewExponents[i] = exponentZn.getUniformlyRandomElement();
        }
        fewPerfTestExpr = new GroupPowExpr(fewBases[0].expr(),
                fewExponents[0].asExponentExpression());
        for (int i = 1; i < fewExponents.length; ++i) {
            fewPerfTestExpr = fewPerfTestExpr
                    .opPow(fewBases[i % fewNumBases], fewExponents[i]);
        }
        fewExprResult = fewPerfTestExpr.evaluate();
    }

    @Test
    public void testCorrectnessCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setForcedMultiExpAlgorithm(algSetting);
        assertEquals(manyExprResult, manyPerfTestExpr.evaluate(evaluator));
    }

    @Test
    public void testCorrectnessNoCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCachingForAlg(algSetting, false);
        evaluator.setForcedMultiExpAlgorithm(algSetting);
        assertEquals(manyExprResult, manyPerfTestExpr.evaluate(evaluator));
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testManyBasesOptCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setForcedMultiExpAlgorithm(algSetting);
        manyPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testManyBasesOptNoCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCachingForAlg(algSetting, false);
        evaluator.setForcedMultiExpAlgorithm(algSetting);
        manyPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testFewBasesOptCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setForcedMultiExpAlgorithm(algSetting);
        fewPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testFewBasesOptNoCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCachingForAlg(algSetting, false);
        evaluator.setForcedMultiExpAlgorithm(algSetting);
        fewPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testManyBasesNaivePerf() {
        manyPerfTestExpr.evaluate();
    }

    @Test
    @JUnitPerfTest(durationMs = perfDuration, warmUpMs = warmupDuration)
    public void testFewBasesNaivePerf() {
        fewPerfTestExpr.evaluate();
    }
}

