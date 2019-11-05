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

    @Rule
    public JUnitPerfRule perfTestRule = new JUnitPerfRule();

    static String modulo = "35201546659608842026088328007565866231962578784643756647773" +
            "109869245232364730066609837018108561065242031153677";
    static final Zp zp = new Zp(new BigInteger(modulo));
    static final Zn exponentZn = new Zn(new BigInteger(modulo).subtract(BigInteger.ONE));
    static RingUnitGroup.RingUnitGroupElement[] bases;
    static Zn.ZnElement[] exponents;

    static GroupElementExpression perfTestExpr ;

    @BeforeClass
    public static void setupPerfTest() {
        bases = new RingUnitGroup.RingUnitGroupElement[10];
        for (int i = 0; i < bases.length; ++i) {
            bases[i] = zp.getUniformlyRandomUnit().toUnitGroupElement();
        }
        exponents = new Zn.ZnElement[10];
        for (int i = 0; i < exponents.length; ++i) {
            exponents[i] = exponentZn.getUniformlyRandomElement();
        }
        perfTestExpr = new GroupPowExpr(bases[0].expr(), exponents[0].asExponentExpression());
        for (int i = 1; i < bases.length; ++i) {
            perfTestExpr.opPow(bases[i], exponents[i]);
        }
    }

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

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testOptPerf() {
        perfTestExpr.evaluate(new OptGroupElementExpressionEvaluator());
    }


    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testNaivePerf() {
        perfTestExpr.evaluate();
    }

}
