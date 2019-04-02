package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.upb.crypto.math.swante.MyExponentiationAlgorithms.*;
import static de.upb.crypto.math.swante.misc.pln;

public class MultiExponentiationTests {
    
    MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
    MyProjectiveCurve curve = new MyProjectiveCurve(parameters);
    AbstractEllipticCurvePoint g = curve.getGenerator();
    
    int numBases = 10;
    List<AbstractEllipticCurvePoint> bases = IntStream.range(0, numBases).mapToObj(it -> curve.getUniformlyRandomElement()).collect(Collectors.toList());
    List<BigInteger> exponents = IntStream.range(0, numBases).mapToObj(it -> misc.randBig(parameters.p)).collect(Collectors.toList());
    
    @Test
    public void testCorrectness() {
        AbstractEllipticCurvePoint expected = curve.getNeutralElement();
        for (int i = 0; i < numBases; i++) {
            expected = expected.op(bases.get(i).pow(exponents.get(i)));
        }
        Assert.assertEquals(expected);
    }
    
    @Test
    public void testPerformance() {
        int numIterations = 10;
        
        for (int windowSize = 1; windowSize < 12; windowSize++) {
            int m = (1 << windowSize) - 1;
            pln("==========================");
            pln(String.format("wsize=%d (m=%d), #bases=%d, #exponents=%d", windowSize, m, numBases, numExponents));
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
                    GroupElement[] smallPowers = precomputeSmallOddPowers(base, m);
                    powUsingSlidingWindow(base, exponents.get(j), windowSize, smallPowers);
                }
            }
            pln(String.format("sliding window pow (without caching) -> %.2f ms", misc.tick()));
            misc.tick();
            for (int i = 0; i < numBases; i++) {
                AbstractEllipticCurvePoint base = bases.get(i);
                GroupElement[] smallPowers = precomputeSmallOddPowers(base, m);
                for (int j = 0; j < numExponents; j++) {
                    powUsingSlidingWindow(base, exponents.get(j), windowSize, smallPowers);
                }
            }
            pln(String.format("sliding window pow (with caching of small base powers) -> %.2f ms", misc.tick()));
            misc.tick();
            for (int i = 0; i < numBases; i++) {
                AbstractEllipticCurvePoint base = bases.get(i);
                GroupElement[] smallPowers = precomputeSmallOddPowers(base, m);
                for (int j = 0; j < numExponents; j++) {
                    int[] expDigits = MyExponentiationAlgorithms.precomputeExponentTransformationForLrSfwMethod(exponents.get(j), m);
                    powUsingLrSfwMethod(base, expDigits, smallPowers);
                }
            }
            pln(String.format("signed digit fractional window pow (with caching of small base powers) -> %.2f ms", misc.tick()));
        }
    }
}
