package de.upb.crypto.math.performance.expressions;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.group.OptGroupElementExpressionEvaluator
        .ForceMultiExpAlgorithmSetting;
import de.upb.crypto.math.expressions.group.PairingExpr;
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

import static junit.framework.TestCase.fail;

@RunWith(Parameterized.class)
public class OptGroupElementExpressionEvaluatorPrecomputeTest {

    @Parameterized.Parameters(name= "{index}: algorithm={0}")
    public static Iterable<OptGroupElementExpressionEvaluator.ForceMultiExpAlgorithmSetting>
    algs() {
        return Arrays.asList(
                ForceMultiExpAlgorithmSetting.INTERLEAVED_SLIDING,
                ForceMultiExpAlgorithmSetting.INTERLEAVED_WNAF,
                ForceMultiExpAlgorithmSetting.SIMULTANEOUS
        );
    }

    @Parameterized.Parameter
    public ForceMultiExpAlgorithmSetting algSetting;

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
        expr = expr.opPow(elem.pow(2), BigInteger.valueOf(2));
        expr = expr.inv();
        expr = expr.opPow(elem.pow(3), BigInteger.valueOf(3));

        expr = expr.op(new PairingExpr(bilGroup.getBilinearMap(), leftExpr, rightExpr));
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setForcedMultiExpAlgorithm(algSetting);
        evaluator.precompute(expr);

        List<GroupElement> bases = new LinkedList<>();
        bases.add(elem.pow(2));
        bases.add(elem);
        bases.add(elem.pow(3));
        GroupPrecomputations gp = GroupPrecomputationsFactory.get(bilGroup.getGT());
        // now check whether bases have been precomputed
        if (algSetting == ForceMultiExpAlgorithmSetting.INTERLEAVED_SLIDING) {
            for (GroupElement b : bases) {
                try {
                    gp.getOddPowers(b, evaluator.getWindowSizeInterleavedSlidingCaching(), false);
                } catch (IllegalStateException e) {
                    System.out.println(e.getMessage());
                    fail("Missing odd powers.");
                }
            }
        } else if (algSetting == ForceMultiExpAlgorithmSetting.INTERLEAVED_WNAF) {
            for (GroupElement b : bases) {
                try {
                    gp.getOddPowers(b, evaluator.getWindowSizeInterleavedWnafCaching(), false);
                } catch (IllegalStateException e) {
                    System.out.println(e.getMessage());
                    fail("Missing odd powers.");
                }
            }
        } else {
            try {
                gp.getPowerProducts(bases, evaluator.getWindowSizeSimultaneousCaching(), false);
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
                fail("Missing power products.");
            }
        }
    }
}
