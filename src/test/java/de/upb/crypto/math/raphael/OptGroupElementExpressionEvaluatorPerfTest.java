package de.upb.crypto.math.raphael;

import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
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

    static RingUnitGroup.RingUnitGroupElement[] interleavedBases;
    static Zn.ZnElement[] interleavedExponents;
    static GroupElementExpression interleavedPerfTestExpr;

    static RingUnitGroup.RingUnitGroupElement[] simultaneousBases;
    static Zn.ZnElement[] simultaneousExponents;
    static GroupElementExpression simultaneousPerfTestExpr;


    @BeforeClass
    public static void setupPerfTest() {
        int interleavedNumBases = 13;
        int interleavedNumExponents = 20;
        interleavedBases = new RingUnitGroup.RingUnitGroupElement[interleavedNumBases];
        for (int i = 0; i < interleavedBases.length; ++i) {
            interleavedBases[i] = zp.getUniformlyRandomUnit().toUnitGroupElement();
        }
        interleavedExponents = new Zn.ZnElement[interleavedNumExponents];
        for (int i = 0; i < interleavedExponents.length; ++i) {
            interleavedExponents[i] = exponentZn.getUniformlyRandomElement();
        }
        interleavedPerfTestExpr = new GroupPowExpr(interleavedBases[0].expr(),
                interleavedExponents[0].asExponentExpression());
        for (int i = 1; i < interleavedExponents.length; ++i) {
            interleavedPerfTestExpr.opPow(interleavedBases[i % interleavedNumBases],
                    interleavedExponents[i]);
        }

        int simultaneousNumBases = 4;
        int simultaneousNumExponents = 20;
        simultaneousBases = new RingUnitGroup.RingUnitGroupElement[simultaneousNumBases];
        for (int i = 0; i < simultaneousBases.length; ++i) {
            simultaneousBases[i] = zp.getUniformlyRandomUnit().toUnitGroupElement();
        }
        simultaneousExponents = new Zn.ZnElement[simultaneousNumExponents];
        for (int i = 0; i < simultaneousExponents.length; ++i) {
            simultaneousExponents[i] = exponentZn.getUniformlyRandomElement();
        }
        simultaneousPerfTestExpr = new GroupPowExpr(simultaneousBases[0].expr(),
                simultaneousExponents[0].asExponentExpression());
        for (int i = 1; i < simultaneousExponents.length; ++i) {
            simultaneousPerfTestExpr.opPow(simultaneousBases[i % simultaneousNumBases],
                    simultaneousExponents[i]);
        }
    }

    @Test
    public void testInterleavedCorrectnessCaching() {
        assertEquals(interleavedPerfTestExpr.evaluate(),
                interleavedPerfTestExpr.evaluate(new OptGroupElementExpressionEvaluator()));
    }

    @Test
    public void testInterleavedCorrectnessNoCaching() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCaching(false);
        assertEquals(interleavedPerfTestExpr.evaluate(),
                interleavedPerfTestExpr.evaluate(evaluator));
    }

    @Test
    public void testSimultaneousCorrectness() {
        // use simultaneous automatically for less than 10 bases and caching enabled
        assertEquals(simultaneousPerfTestExpr.evaluate(),
                simultaneousPerfTestExpr.evaluate(new OptGroupElementExpressionEvaluator()));
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testInterleavedOptCachingPerf() {
        interleavedPerfTestExpr.evaluate(new OptGroupElementExpressionEvaluator());
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testInterleavedOptNoCachingPerf() {
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.setEnableCaching(false);
        interleavedPerfTestExpr.evaluate(evaluator);
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testInterleavedNaivePerf() {
        interleavedPerfTestExpr.evaluate();
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testSimultaneousPerf() {
        simultaneousPerfTestExpr.evaluate(new OptGroupElementExpressionEvaluator());
    }

    @Test
    @JUnitPerfTest(durationMs = 15_000, warmUpMs = 5_000)
    public void testSimultaneousNaivePerf() {
        simultaneousPerfTestExpr.evaluate();
    }
}
