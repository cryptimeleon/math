package de.upb.crypto.math.raphael;

import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.*;

import java.math.BigInteger;

public class SingleExponentiationTest {

    @Rule
    public JUnitPerfRule perfTestRule = new JUnitPerfRule();

    static String modulo = "35201546659608842026088328007565866231962578784643756647773" +
            "109869245232364730066609837018108561065242031153677";
    static final Zp zp = new Zp(new BigInteger(modulo));
    static final Zn exponentZn = new Zn(new BigInteger(modulo).subtract(BigInteger.ONE));
    static Zp.ZpElement base;
    static Zn.ZnElement[] exponents;

    @BeforeClass
    public static void setup() {
        base = zp.getUniformlyRandomUnit();
        exponents = new Zn.ZnElement[20];
        for (int i = 0; i < exponents.length; ++i) {
            exponents[i] = exponentZn.getUniformlyRandomElement();
        }
    }

    @Test
    @JUnitPerfTest(durationMs = 10_000, warmUpMs = 5_000)
    public void defaultPow() {
        GroupElement result = base.toUnitGroupElement();
        for (Zn.ZnElement exp : exponents) {
            result = result.pow(exp.getInteger());
        }
    }

    @Test
    @JUnitPerfTest(durationMs = 10_000, warmUpMs = 5_000)
    public void slidingPow() {
        // use optimal value 4
        GroupElement result = base.toUnitGroupElement();
        for (Zn.ZnElement exp : exponents) {
            result = result.powSlidingWindow(exp.getInteger(), 4, false);
        }
    }

    @Test
    @JUnitPerfTest(durationMs = 10_000, warmUpMs = 5_000)
    public void slidingPowCaching() {
        // use optimal value 4
        GroupElement result = base.toUnitGroupElement();
        for (Zn.ZnElement exp : exponents) {
            result = result.powSlidingWindow(exp.getInteger(), 8, true);
        }
    }
}
