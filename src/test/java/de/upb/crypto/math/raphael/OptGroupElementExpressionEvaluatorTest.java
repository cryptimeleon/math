package de.upb.crypto.math.raphael;

import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupInvExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;

import java.math.BigInteger;

public class OptGroupElementExpressionEvaluatorTest {

    @Test
    public void test() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        GroupElement elem = zp.getOneElement().toAdditiveGroupElement();

        GroupElementExpression expr = elem.expr();
        expr = expr.opPow(elem.pow(2), BigInteger.valueOf(2));
        expr = expr.inv();
        expr = expr.opPow(elem.pow(3), BigInteger.valueOf(3));
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        expr.evaluate(evaluator);
    }
}
