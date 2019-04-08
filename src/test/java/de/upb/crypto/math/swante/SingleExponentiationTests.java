package de.upb.crypto.math.swante;

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

public class SingleExponentiationTests {
    
    MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
    MyProjectiveCurve curve = new MyProjectiveCurve(parameters);
    
    @Test
    public void testCorrectness() {
        AbstractEllipticCurvePoint g = curve.getGenerator();
        GroupElement[] expected = {g.pow(1), g.pow(3), g.pow(5), g.pow(7)};
        int windowSize = 3;
        int m = (1 << windowSize) - 1;
        GroupElement[] smallPowersOfG = precomputeSmallOddPowers(g, m);
        Assert.assertArrayEquals(expected, smallPowersOfG);
        BigInteger exponent = BigInteger.valueOf(1000001);
        Assert.assertEquals(MyExponentiationAlgorithms.simpleSquareAndMultiplyPow(g, exponent), powUsingSlidingWindow(g, exponent, windowSize, smallPowersOfG));
        Assert.assertArrayEquals(new int[]{-31,0,0,0,0,0,0,-17,0,0,0,0,0,3}, MyExponentiationAlgorithms.precomputeExponentDigitsForWNAF(BigInteger.valueOf(22369), 5));
        Assert.assertArrayEquals(new int[]{1,0,0,0,0,-5,0,0,0,0,0,1,5}, MyExponentiationAlgorithms.precomputeExponentTransformationForLrSfwMethod(BigInteger.valueOf(22369), 5));
        int[] expDigits = MyExponentiationAlgorithms.precomputeExponentTransformationForLrSfwMethod(exponent, m);
        Assert.assertEquals(MyExponentiationAlgorithms.simpleSquareAndMultiplyPow(g, exponent), powUsingLrSfwMethod(g, expDigits, smallPowersOfG));
    }
    
    @Test
    public void testPerformance() {
        int numBases = 10;
        int numExponents = 20;
        List<AbstractEllipticCurvePoint> bases = IntStream.range(0, numBases).mapToObj(it -> curve.getUniformlyRandomElement()).collect(Collectors.toList());
        List<BigInteger> exponents = IntStream.range(0, numExponents).mapToObj(it -> misc.randBig(parameters.p)).collect(Collectors.toList());
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
            powSimpleSlidignWindowOpCounter = 0;
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
                GroupElement[] smallOddPowers = precomputeSmallOddPowers(base, m);
                for (int j = 0; j < numExponents; j++) {
                    int[] expDigits = MyExponentiationAlgorithms.precomputeExponentDigitsForWNAF(exponents.get(j), windowSize);
                    MyExponentiationAlgorithms.powSingleWNaf(base, expDigits, smallOddPowers);
                }
            }
            pln(String.format("signed digit / (non-fractional!) wNAF pow (with caching of small base powers) -> %.2f ms", misc.tick()));
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
            pln("total #M, simple Squre&Multiply: " + powSimpleSquareAndMultiplyOpCounter);
            pln("total #M, simple SlidingWindow: " + powSimpleSlidignWindowOpCounter);
            pln("total #M, wNAF: " + powSingleWNafOpCounter);
            pln("total #M, fractional windows: " + powUsingLrSfwMethodOpCounter);
            powSimpleSquareAndMultiplyOpCounter = 0;
            powSimpleSlidignWindowOpCounter = 0;
            powSingleWNafOpCounter = 0;
            powUsingLrSfwMethodOpCounter = 0;
        }
        
    }
}
