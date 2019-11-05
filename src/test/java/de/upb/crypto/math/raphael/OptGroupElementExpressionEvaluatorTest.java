package de.upb.crypto.math.raphael;

import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class OptGroupElementExpressionEvaluatorTest {

    @Test
    public void testOpPowInv() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        GroupElement elem = zp.getOneElement().toAdditiveGroupElement();

        GroupElementExpression expr = elem.expr();
        expr = expr.opPow(elem.pow(2), BigInteger.valueOf(2));
        expr = expr.inv();
        expr = expr.opPow(elem.pow(3), BigInteger.valueOf(3));
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        assertEquals(expr.evaluate(), expr.evaluate(evaluator));
    }


}
