package de.upb.crypto.math.expressions.test;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.exponent.ZnExponentAsAdditiveGroupElemExpr;
import de.upb.crypto.math.expressions.exponent.ZnExponentAsMultiplicativeGroupElemExpr;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupVariableExpr;
import de.upb.crypto.math.expressions.group.PairingExpr;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;
import org.junit.Test;

import java.math.BigInteger;

public class OptGroupElementExpressionEvaluatorEvaluateTest {

    @Test(expected = EvaluationException.class)
    public void testVarExpr() {
        GroupVariableExpr expr = new GroupVariableExpr("x");
        expr.evaluate();
    }

    @Test
    public void testMultithreadedPairingEval() {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setDebugMode(true);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup bilGroup = fac.createBilinearGroup();

        GroupElementExpression expr = new PairingExpr(
                bilGroup.getBilinearMap(),
                bilGroup.getG1().getNeutralElement().expr(),
                bilGroup.getG2().getNeutralElement().expr()
        );

        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setEnableMultithreadedPairingEvaluation(true);
        assert expr.evaluate(evaluator).equals(expr.evaluateNaive());
    }

    @Test
    public void testZnExponentAsAdditiveGroupElemExpr() {
        Zn zn = new Zn(BigInteger.valueOf(100));
        ExponentExpr expExpr = new ExponentConstantExpr(zn.getUniformlyRandomElement());
        expExpr = expExpr.add(expExpr);
        GroupElementExpression expr = new ZnExponentAsAdditiveGroupElemExpr(zn, expExpr);
        expr = expr.op(expr);
        expr = expr.opPow(expr, expExpr);
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        GroupElement naiveResult = expr.evaluateNaive();
        assert evaluator.evaluate(expr).equals(naiveResult);
        GroupElementExpression precExpr = evaluator.precompute(expr);
        assert evaluator.evaluate(precExpr).equals(naiveResult);
    }

    @Test
    public void testZnExponentAsMultiplicativeGroupElemExpr() {
        Zn zn = new Zn(BigInteger.valueOf(100));
        ExponentExpr expExpr = new ExponentConstantExpr(zn.getUniformlyRandomUnit());
        expExpr = expExpr.add(expExpr);
        GroupElementExpression expr = new ZnExponentAsMultiplicativeGroupElemExpr(zn, expExpr);
        expr = expr.op(expr);
        expr = expr.opPow(expr, expExpr);
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        GroupElement naiveResult = expr.evaluateNaive();
        assert evaluator.evaluate(expr).equals(naiveResult);
        GroupElementExpression precExpr = evaluator.precompute(expr);
        assert evaluator.evaluate(precExpr).equals(naiveResult);
    }
}
