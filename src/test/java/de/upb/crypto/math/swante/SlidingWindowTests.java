package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.upb.crypto.math.swante.misc.pln;

public class SlidingWindowTests {
    
    MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
    MyProjectiveCurve curve = new MyProjectiveCurve(parameters);
    
    @Test
    public void testCorrectness() {
        AbstractEllipticCurvePoint g = curve.getGenerator();
        GroupElement[] expected = {g.pow(1), g.pow(3), g.pow(5), g.pow(7)};
        int windowSize = 3;
        GroupElement[] smallPowersOfG = g.precomputePowersForSlidingWindow(windowSize);
        Assert.assertArrayEquals(expected,smallPowersOfG);
        BigInteger exponent = BigInteger.valueOf(1000001);
        Assert.assertEquals(g.pow(exponent), g.powUsingSlidingWindow(exponent, windowSize, smallPowersOfG));
    }
    
    @Test
    public void testPerformance() {
        int numBases = 10;
        int numExponents = 20;
        List<AbstractEllipticCurvePoint> bases = IntStream.range(0, numBases).mapToObj(it -> curve.getUniformlyRandomElement()).collect(Collectors.toList());
        List<BigInteger> exponents = IntStream.range(0, numExponents).mapToObj(it -> misc.randBig(parameters.p)).collect(Collectors.toList());
        for (int windowSize = 1; windowSize < 12; windowSize++) {
            pln("==========================");
            pln(String.format("wsize=%d, #bases=%d, #exponents=%d", windowSize, numBases, numExponents));
            misc.tick();
            for (int i = 0; i < numBases; i++) {
                AbstractEllipticCurvePoint base = bases.get(i);
                for (int j = 0; j < numExponents; j++) {
                    base.pow(exponents.get(j));
                }
            }
            pln(String.format("normal pow -> %.2f ms", misc.tick()));
            misc.tick();
            for (int i = 0; i < numBases; i++) {
                AbstractEllipticCurvePoint base = bases.get(i);
                for (int j = 0; j < numExponents; j++) {
                    GroupElement[] smallPowers = base.precomputePowersForSlidingWindow(windowSize);
                    base.powUsingSlidingWindow(exponents.get(j), windowSize, smallPowers);
                }
            }
            pln(String.format("simple sliding window pow -> %.2f ms", misc.tick()));
            misc.tick();
            for (int i = 0; i < numBases; i++) {
                AbstractEllipticCurvePoint base = bases.get(i);
                GroupElement[] smallPowers = base.precomputePowersForSlidingWindow(windowSize);
                for (int j = 0; j < numExponents; j++) {
                    base.powUsingSlidingWindow(exponents.get(j), windowSize, smallPowers);
                }
            }
            pln(String.format("sliding window pow (with caching) -> %.2f ms", misc.tick()));
        }
    }
}
