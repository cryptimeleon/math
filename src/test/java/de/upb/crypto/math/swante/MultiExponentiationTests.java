package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;
import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.powproducts.*;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.stream.IntStream;

import static de.upb.crypto.math.swante.misc.pln;

public class MultiExponentiationTests {
    
        MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
    MyProjectiveCurve curve = new MyProjectiveCurve(parameters);
    BigInteger p = parameters.p;
//    BigInteger p = BigInteger.valueOf(19);
//    RingUnitGroup curve = new Zp(p).asUnitGroup();
    
    
    @Test
    public void testCorrectness() {
        int numBases = 2;
        GroupElement[] bases = IntStream.range(0, numBases).mapToObj(it -> curve.getUniformlyRandomElement()).toArray(GroupElement[]::new);
        BigInteger[] exponents = IntStream.range(0, numBases).mapToObj(it -> misc.randBig(p)).toArray(BigInteger[]::new);
        exponents[0] = BigInteger.valueOf(2);
        exponents[1] = BigInteger.valueOf(3);
        PowProductExpression originalPowProductExpression = new PowProductExpression(curve);
        pln(bases);
        pln(exponents);
        GroupElement expected = curve.getNeutralElement();
        for (int i = 0; i < numBases; i++) {
            GroupElement base = bases[i];
            BigInteger e = exponents[i];
            expected = expected.op(base.pow(e));
            originalPowProductExpression.op(base, e);
        }
        Assert.assertEquals(expected, curve.evaluate(originalPowProductExpression));
        Assert.assertEquals(expected, new MyArrayPowProductWithFixedBases(bases).evaluate(exponents));
        Assert.assertEquals(expected, new MyFastPowProductWithoutCaching(bases).evaluate(exponents));
        Assert.assertEquals(expected, new MySimultaneous2wAryPowProduct(bases, 3).evaluate(exponents));
        Assert.assertEquals(expected, new MySimultaneousSlidingWindowPowProduct(bases, 3).evaluate(exponents));
        Assert.assertEquals(expected, new MySimpleInterleavingPowProduct(bases, 3).evaluate(exponents));
        Assert.assertEquals(expected, new MyInterleavingSignedWindowPowProduct(bases, 3).evaluate(exponents));
    }
    
    @Test
    public void testPerformance() {
        int numIterations = 100;
        int numBases = 10;
        int simultaneousWindowSize = 1;
        int interleavingWindowSize = 2;
        
        GroupElement[] bases = IntStream.range(0, numBases).mapToObj(it -> curve.getUniformlyRandomElement()).toArray(GroupElement[]::new);
        pln(bases);
        BigInteger[][] exponents = new BigInteger[numIterations][];
        for (int i = 0; i < numIterations; i++) {
            exponents[i] = IntStream.range(0, numBases).mapToObj(it -> misc.randBig(p)).toArray(BigInteger[]::new);
//            pln(exponents[i]);
        }
        
        GroupElement[] expected = new GroupElement[numIterations];

        pln("==========================");
        pln(String.format("#iterations=%d, #bases=%d, windowSize=%d", numIterations, numBases, simultaneousWindowSize));
        misc.tick();
        MyArrayPowProductWithFixedBases my1 = new MyArrayPowProductWithFixedBases(bases);
        for (int i = 0; i < numIterations; i++) {
            expected[i] = my1.evaluate(exponents[i]);
        }
        pln(String.format("simple array multi exponentiation -> %.2f ms", misc.tick()));
        misc.tick();
        for (int i = 0; i < numIterations; i++) {
            PowProductExpression originalPowProductExpression = new PowProductExpression(curve);
            for (int b = 0; b < numBases; b++) {
                GroupElement base = bases[b];
                BigInteger e = exponents[i][b];
                originalPowProductExpression.op(base, e);
            }
            Assert.assertEquals(expected[i],  curve.evaluate(originalPowProductExpression));
        }
        pln(String.format("original power product -> %.2f ms", misc.tick()));
        misc.tick();
        MyFastPowProductWithoutCaching my2 = new MyFastPowProductWithoutCaching(bases);
        for (int i = 0; i < numIterations; i++) {
            Assert.assertEquals(expected[i], my2.evaluate(exponents[i]));
        }
        pln(String.format("fast multi exponentiation without caching -> %.2f ms", misc.tick()));
        misc.tick();
        MySimultaneous2wAryPowProduct my3 = new MySimultaneous2wAryPowProduct(bases, simultaneousWindowSize);
        for (int i = 0; i < numIterations; i++) {
            Assert.assertEquals(expected[i], my3.evaluate(exponents[i]));
        }
        pln(String.format("simultaneous 2w-ary -> %.2f ms", misc.tick()));
        misc.tick();
        MySimultaneousSlidingWindowPowProduct my4 = new MySimultaneousSlidingWindowPowProduct(bases, simultaneousWindowSize);
        for (int i = 0; i < numIterations; i++) {
            Assert.assertEquals(expected[i], my4.evaluate(exponents[i]));
        }
        pln(String.format("simultaneous sliding-window -> %.2f ms", misc.tick()));
        misc.tick();
        MySimpleInterleavingPowProduct my5 = new MySimpleInterleavingPowProduct(bases, interleavingWindowSize);
        for (int i = 0; i < numIterations; i++) {
            Assert.assertEquals(expected[i], my5.evaluate(exponents[i]));
        }
        pln(String.format("interleaving sliding-window -> %.2f ms", misc.tick()));
    }
}
