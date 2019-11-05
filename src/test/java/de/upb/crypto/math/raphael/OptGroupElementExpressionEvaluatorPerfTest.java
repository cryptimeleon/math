package de.upb.crypto.math.raphael;

import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class OptGroupElementExpressionEvaluatorPerfTest {

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
        bases = new RingUnitGroup.RingUnitGroupElement[20];
        for (int i = 0; i < bases.length; ++i) {
            bases[i] = zp.getUniformlyRandomUnit().toUnitGroupElement();
        }
        exponents = new Zn.ZnElement[20];
        for (int i = 0; i < exponents.length; ++i) {
            exponents[i] = exponentZn.getUniformlyRandomElement();
        }
        perfTestExpr = new GroupPowExpr(bases[0].expr(), exponents[0].asExponentExpression());
        for (int i = 1; i < bases.length; ++i) {
            perfTestExpr.opPow(bases[i], exponents[i]);
        }
    }

    @Test
    public void testCorrectnessCaching() {
        assertEquals(perfTestExpr.evaluate(),
                perfTestExpr.evaluate(new OptGroupElementExpressionEvaluator()));
    }

    @Test
    public void testCorrectnessNoCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCaching(false);
        assertEquals(perfTestExpr.evaluate(),
                perfTestExpr.evaluate(evaluator));
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testOptCachingPerf() {
        perfTestExpr.evaluate(new OptGroupElementExpressionEvaluator());
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testOptNoCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCaching(false);
        perfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testNaivePerf() {
        perfTestExpr.evaluate();
    }
}
