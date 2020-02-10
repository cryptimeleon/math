package de.upb.crypto.math.expressions.test;

import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluatorConfig;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.*;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory.GroupPrecomputations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(Parameterized.class)
public class OptGroupElementExpressionEvaluatorPrecomputeTest {

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
    public void testPrecomputingPowers() {
        BilinearGroupFactory fac = new BilinearGroupFactory(160);
        fac.setDebugMode(true);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup bilGroup = fac.createBilinearGroup();
        GroupElement leftElem = bilGroup.getG1().getUniformlyRandomElement();
        System.out.println("Left Element G1: " + leftElem);
        GroupElement rightElem = bilGroup.getG2().getUniformlyRandomElement();
        System.out.println("Right Element G2: " + rightElem);

        GroupElementExpression leftExpr = leftElem.expr();
        leftExpr = leftExpr.opPow(leftElem.pow(2), BigInteger.valueOf(2));
        leftExpr = leftExpr.inv();
        GroupElementExpression rightExpr = rightElem.expr();
        rightExpr = rightExpr.opPow(rightElem.pow(3), BigInteger.valueOf(3));
        rightExpr = rightExpr.inv();

        GroupElement elem = bilGroup.getGT().getUniformlyRandomNonNeutral();
        System.out.println("Element: " + elem);
        GroupElementExpression expr = elem.expr();
        expr = expr.opPow(elem.pow(2), new ExponentVariableExpr("x"));
        expr = expr.inv();
        expr = expr.opPow(elem.pow(3), new ExponentVariableExpr("y"));

        expr = expr.op(new PairingExpr(bilGroup.getBilinearMap(), leftExpr, rightExpr));
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        evaluator.getConfig().setEnablePrecomputeEvaluation(false);
        evaluator.precompute(expr);

        List<GroupElement> bases = new LinkedList<>();
        bases.add(elem.pow(2));
        bases.add(elem);
        bases.add(elem.pow(3));
        GroupPrecomputations gp = GroupPrecomputationsFactory.get(bilGroup.getGT());
        // now check whether bases have been precomputed
        if (algSetting == OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_SLIDING) {
            for (GroupElement b : bases) {
                try {
                    gp.getOddPowers(b,
                            evaluator.getConfig().getWindowSizeInterleavedSlidingCaching(), false);
                } catch (IllegalStateException e) {
                    System.out.println(e.getMessage());
                    fail("Missing odd powers for " + b);
                }
            }
        } else if (algSetting == OptGroupElementExpressionEvaluatorConfig.ForceMultiExpAlgorithmSetting.INTERLEAVED_WNAF) {
            for (GroupElement b : bases) {
                try {
                    gp.getOddPowers(b, evaluator.getConfig().getWindowSizeInterleavedWnafCaching(), false);
                } catch (IllegalStateException e) {
                    System.out.println(e.getMessage());
                    fail("Missing odd powers for " + b);
                }
            }
        } else {
            try {
                gp.getPowerProducts(bases, evaluator.getConfig().getWindowSizeSimultaneousCaching(), false);
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
                fail("Missing power products for " + Arrays.toString(bases.toArray()));
            }
        }
    }

    @Test
    public void testPrecomputeEvaluation() {
        // Just reuse from PrecomputePowers test
        BilinearGroupFactory fac = new BilinearGroupFactory(160);
        fac.setDebugMode(true);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup bilGroup = fac.createBilinearGroup();
        GroupElement leftElem = bilGroup.getG1().getUniformlyRandomElement();
        System.out.println("Left Element G1: " + leftElem);
        GroupElement rightElem = bilGroup.getG2().getUniformlyRandomElement();
        System.out.println("Right Element G2: " + rightElem);

        GroupElementExpression leftExpr = leftElem.expr();
        leftExpr = leftExpr.opPow(leftElem.pow(2), BigInteger.valueOf(2));
        leftExpr = leftExpr.inv();
        GroupElementExpression rightExpr = rightElem.expr();
        rightExpr = rightExpr.opPow(rightElem.pow(3), BigInteger.valueOf(3));
        rightExpr = rightExpr.inv();

        GroupElement elem = bilGroup.getGT().getUniformlyRandomNonNeutral();
        System.out.println("Element: " + elem);
        GroupElementExpression expr = elem.expr();
        expr = expr.opPow(elem.pow(2), new ExponentVariableExpr("x"));
        expr = expr.inv();
        expr = expr.opPow(elem.pow(3), new ExponentVariableExpr("y"));

        PairingExpr pairingExpr = new PairingExpr(bilGroup.getBilinearMap(), leftExpr, rightExpr);
        expr = expr.op(pairingExpr);

        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setForcedMultiExpAlgorithm(algSetting);
        GroupElementExpression newExpr = evaluator.precompute(expr);

        GroupOpExpr newOpExpr = (GroupOpExpr) newExpr;
        assertTrue(newOpExpr.getRhs() instanceof GroupElementConstantExpr);
        assertEquals(newOpExpr.getRhs().evaluate(), pairingExpr.evaluate());
    }
}
