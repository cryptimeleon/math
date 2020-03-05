package de.upb.crypto.math.expressions.test;

import de.upb.crypto.math.expressions.evaluator.NaiveGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluatorConfig;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.performance.expressions.ExpressionGenerator;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class OptGroupElementExpressionEvaluatorEvaluateAlgsTest {

    @Parameterized.Parameters(name= "{index}: algorithm={0}")
    public static Iterable<OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting>
    algs() {
        return Arrays.asList(
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_SLIDING,
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_WNAF,
                OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.SIMULTANEOUS
        );
    }

    @Parameterized.Parameter
    public OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting algSetting;

    @Test
    public void testAdditiveMultiExp() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        GroupElementExpression expr = ExpressionGenerator
                .genMultiExponentiation(zp.asAdditiveGroup(), 10, 15);
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        NaiveGroupElementExpressionEvaluator naiveEval = new NaiveGroupElementExpressionEvaluator();
        assertEquals(expr.evaluate(naiveEval), expr.evaluate(evaluator));
    }

    @Test
    public void testMultiplicativeMultiExp() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        GroupElementExpression expr = ExpressionGenerator
                .genMultiExponentiation(zp.asUnitGroup(), 10, 15);
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        NaiveGroupElementExpressionEvaluator naiveEval = new NaiveGroupElementExpressionEvaluator();
        assertEquals(expr.evaluate(naiveEval), expr.evaluate(evaluator));
    }

    @Test
    public void testPairingWithMultiExpMultithreaded() {
        BilinearGroupFactory fac = new BilinearGroupFactory(160);
        fac.setDebugMode(true);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        GroupElementExpression expr = ExpressionGenerator.genPairingWithMultiExp(
                fac.createBilinearGroup().getBilinearMap(),
                5,
                5,
                5,
                5
        );

        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        NaiveGroupElementExpressionEvaluator naiveEval = new NaiveGroupElementExpressionEvaluator();
        assertEquals(expr.evaluate(naiveEval), expr.evaluate(evaluator));
    }

    @Test
    public void testPairingWithMultiExpNotMultithreaded() {
        BilinearGroupFactory fac = new BilinearGroupFactory(160);
        fac.setDebugMode(true);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        GroupElementExpression expr = ExpressionGenerator.genPairingWithMultiExp(
                fac.createBilinearGroup().getBilinearMap(),
                5,
                5,
                5,
                5
        );

        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        evaluator.getConfig().setEnableMultithreadedPairingEvaluation(false);
        NaiveGroupElementExpressionEvaluator naiveEval = new NaiveGroupElementExpressionEvaluator();
        assertEquals(expr.evaluate(naiveEval), expr.evaluate(evaluator));
    }
}
