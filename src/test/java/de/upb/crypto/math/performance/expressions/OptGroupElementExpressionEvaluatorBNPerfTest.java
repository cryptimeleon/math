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

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@Ignore
public class OptGroupElementExpressionEvaluatorBNPerfTest {

    @Rule
    public JUnitPerfRule perfTestRule = new JUnitPerfRule();

    static Zn exponentZn;

    static GroupElement[] interleavedBases;
    static Zn.ZnElement[] interleavedExponents;
    static GroupElementExpression interleavedPerfTestExpr;
    static GroupElement interleavedExprResult;

    static GroupElement[] simultaneousBases;
    static Zn.ZnElement[] simultaneousExponents;
    static GroupElementExpression simultaneousPerfTestExpr;
    static GroupElement simultaneousExprResult;

    static BilinearGroup bilGroup;


    @BeforeClass
    public static void setupPerfTest() {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        bilGroup = fac.createBilinearGroup();
        exponentZn = bilGroup.getG1().getZn();
        int interleavedNumBases = 11;
        int interleavedNumExponents = 11;
        interleavedBases = new GroupElement[interleavedNumBases];
        for (int i = 0; i < interleavedBases.length; ++i) {
            interleavedBases[i] = bilGroup.getG1().getUniformlyRandomNonNeutral();
            // Do precomputations before
            GroupPrecomputationsFactory
                    .get(bilGroup.getG1())
                    .getOddPowers(interleavedBases[i], (1<<8)-1);
        }
        interleavedExponents = new Zn.ZnElement[interleavedNumExponents];
        for (int i = 0; i < interleavedExponents.length; ++i) {
            interleavedExponents[i] = exponentZn.getUniformlyRandomElement();
        }
        interleavedPerfTestExpr = new GroupPowExpr(interleavedBases[0].expr(),
                interleavedExponents[0].asExponentExpression());
        for (int i = 1; i < interleavedExponents.length; ++i) {
            interleavedPerfTestExpr = interleavedPerfTestExpr
                    .opPow(interleavedBases[i % interleavedNumBases], interleavedExponents[i]);
        }
        interleavedExprResult = interleavedPerfTestExpr.evaluate();

        int simultaneousNumBases = 6;
        int simultaneousNumExponents = 6;
        simultaneousBases = new GroupElement[simultaneousNumBases];
        for (int i = 0; i < simultaneousBases.length; ++i) {
            simultaneousBases[i] = bilGroup.getG1().getUniformlyRandomNonNeutral();
        }
        // Do precomputations before
        GroupPrecomputationsFactory
                .get(bilGroup.getG1())
                .getPowerProducts(Arrays.asList(simultaneousBases), 1);
        simultaneousExponents = new Zn.ZnElement[simultaneousNumExponents];
        for (int i = 0; i < simultaneousExponents.length; ++i) {
            simultaneousExponents[i] = exponentZn.getUniformlyRandomElement();
        }
        simultaneousPerfTestExpr = new GroupPowExpr(simultaneousBases[0].expr(),
                simultaneousExponents[0].asExponentExpression());
        for (int i = 1; i < simultaneousExponents.length; ++i) {
            simultaneousPerfTestExpr = simultaneousPerfTestExpr
                    .opPow(simultaneousBases[i % simultaneousNumBases], simultaneousExponents[i]);
        }
        simultaneousExprResult = simultaneousPerfTestExpr.evaluate();
    }

    @Test
    public void testInterleavedCorrectnessCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.INTERLEAVED);
        assertEquals(interleavedExprResult, interleavedPerfTestExpr.evaluate(evaluator));
    }

    @Test
    public void testInterleavedCorrectnessNoCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCachingInterleaved(false);
        evaluator.setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.INTERLEAVED);
        assertEquals(interleavedExprResult, interleavedPerfTestExpr.evaluate(evaluator));
    }

    @Test
    public void testSimultaneousCorrectnessCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.SIMULTANEOUS);
        assertEquals(simultaneousExprResult, simultaneousPerfTestExpr.evaluate(evaluator));
    }

    @Test
    public void testSimultaneousCorrectnessNoCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCachingSimultaneous(false);
        evaluator.setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.SIMULTANEOUS);
        assertEquals(simultaneousExprResult, simultaneousPerfTestExpr.evaluate(evaluator));
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testInterleavedOptCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.INTERLEAVED);
        interleavedPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testInterleavedOptNoCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCachingInterleaved(false);
        evaluator.setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.INTERLEAVED);
        interleavedPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testInterleavedNaivePerf() {
        interleavedPerfTestExpr.evaluate();
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testSimultaneousCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.SIMULTANEOUS);
        simultaneousPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testSimultaneousNoCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCachingSimultaneous(false);
        evaluator.setForcedMultiExpAlgorithm(
                OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting.SIMULTANEOUS);
        simultaneousPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testSimultaneousNaivePerf() {
        simultaneousPerfTestExpr.evaluate();
    }
}

