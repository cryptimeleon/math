package de.upb.crypto.math.raphael;

import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.standalone.test.ZpParams;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.*;

import java.math.BigInteger;

//@Ignore
public class SingleExponentiationTest {

    @Rule
    public JUnitPerfRule perfTestRule = new JUnitPerfRule();

    static String modulo = "35201546659608842026088328007565866231962578784643756647773" +
            "109869245232364730066609837018108561065242031153677";
    static final Zp zp = new Zp(new BigInteger(modulo));
    static final Zn exponentZn = new Zn(new BigInteger(modulo).subtract(BigInteger.ONE));
    static RingUnitGroup.RingUnitGroupElement[] bases;
    static Zn.ZnElement[] exponents;

    static BilinearGroup bilGroup;
    static GroupElement bilBase;

    @BeforeClass
    public static void setup() {
        bases = new RingUnitGroup.RingUnitGroupElement[5];
        for (int i = 0; i < bases.length; ++i) {
            bases[i] = zp.getUniformlyRandomUnit().toUnitGroupElement();
        }
        exponents = new Zn.ZnElement[10];
        for (int i = 0; i < exponents.length; ++i) {
            exponents[i] = exponentZn.getUniformlyRandomElement();
        }
        /*BilinearGroupFactory fac = new BilinearGroupFactory(256);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        bilGroup = fac.createBilinearGroup();
        bilBase = bilGroup.getG1().getUniformlyRandomNonNeutral();*/
    }

    @Test
    @JUnitPerfTest(durationMs = 30_000, warmUpMs = 5_000)
    public void defaultPow() {
        //GroupElement result = bilBase.op(bilGroup.getG1().getNeutralElement());
        for (Zn.ZnElement exp : exponents) {
            for (RingUnitGroup.RingUnitGroupElement base : bases) {
                base.pow(exp.getInteger());
            }
        }
    }

    @Test
    @JUnitPerfTest(durationMs = 30_000, warmUpMs = 5_000)
    public void slidingPow() {
        // use optimal value 4
        for (Zn.ZnElement exp : exponents) {
            for (RingUnitGroup.RingUnitGroupElement base : bases) {
                base.powSlidingWindow(exp.getInteger(), 4 , false);
            }
        }
    }

    @Test
    @JUnitPerfTest(durationMs = 30_000, warmUpMs = 5_000)
    public void slidingPowCaching() {
        // use optimal value 4
        for (Zn.ZnElement exp : exponents) {
            for (RingUnitGroup.RingUnitGroupElement base : bases) {
                base.powSlidingWindow(exp.getInteger(), 8, true);
            }
        }
    }
}
