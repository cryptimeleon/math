package de.upb.crypto.math.performance.expressions;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.group.PairingExpr;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class OptGroupElementExpressionEvaluatorTest {

    @Test
    public void testAddOpPowInv() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        GroupElement elem = zp.getOneElement().toAdditiveGroupElement();

        GroupElementExpression expr = elem.expr();
        expr = expr.opPow(elem.pow(2), BigInteger.valueOf(2));
        expr = expr.inv();
        expr = expr.opPow(elem.pow(3), BigInteger.valueOf(3));
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        assertEquals(expr.evaluate(), expr.evaluate(evaluator));
    }

    @Test
    public void testMulOpPowInv() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        GroupElement elem = zp.createZnElement(BigInteger.valueOf(2)).toUnitGroupElement();

        GroupElementExpression expr = elem.expr();
        expr = expr.opPow(elem.pow(2), BigInteger.valueOf(2));
        expr = expr.inv();
        expr = expr.opPow(elem.pow(3), BigInteger.valueOf(3));
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        assertEquals(expr.evaluate(), expr.evaluate(evaluator));
    }

    @Test
    public void testPairing() {
        BilinearGroupFactory fac = new BilinearGroupFactory(160);
        fac.setDebugMode(true);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup bilGroup = fac.createBilinearGroup();
        GroupElement leftElem = bilGroup.getG1().getUniformlyRandomElement();
        System.out.println("Left Element: " + leftElem);
        GroupElement rightElem = bilGroup.getG2().getUniformlyRandomElement();
        System.out.println("Left Element: " + rightElem);

        GroupElementExpression leftExpr = leftElem.expr();
        leftExpr = leftExpr.opPow(leftElem.pow(2), BigInteger.valueOf(2));
        leftExpr = leftExpr.inv();
        GroupElementExpression rightExpr = rightElem.expr();
        rightExpr = rightExpr.opPow(rightElem.pow(3), BigInteger.valueOf(3));
        rightExpr = rightExpr.inv();
        GroupElementExpression expr = new PairingExpr(bilGroup.getBilinearMap(), leftExpr, rightExpr);

        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        assertEquals(expr.evaluate(), expr.evaluate(evaluator));
    }
}
