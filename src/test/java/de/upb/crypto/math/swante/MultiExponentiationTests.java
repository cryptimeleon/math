package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.PowProductExpression;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.upb.crypto.math.swante.misc.pln;

public class MultiExponentiationTests {
    
    MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
    MyProjectiveCurve curve = new MyProjectiveCurve(parameters);
    AbstractEllipticCurvePoint g = curve.getGenerator();
    
    int numBases = 10;
    AbstractEllipticCurvePoint[] bases = IntStream.range(0, numBases).mapToObj(it -> curve.getUniformlyRandomElement()).collect(Collectors.toList()).toArray(new AbstractEllipticCurvePoint[0]);
    BigInteger[] exponents = IntStream.range(0, numBases).mapToObj(it -> misc.randBig(parameters.p)).collect(Collectors.toList()).toArray(new BigInteger[0]);
    PowProductExpression originalPowProductExpression = new PowProductExpression(curve);
    
    @Before
    public void init() {
        for (int i = 0; i < numBases; i++) {
            AbstractEllipticCurvePoint base = bases[i];
            BigInteger e = exponents[i];
            originalPowProductExpression.op(base, e);
        }
    }
    
    @Test
    public void testCorrectness() {
        AbstractEllipticCurvePoint expected = curve.getNeutralElement();
        for (int i = 0; i < numBases; i++) {
            AbstractEllipticCurvePoint base = bases[i];
            BigInteger e = exponents[i];
            expected = expected.op(base.pow(e));
        }
        Assert.assertEquals(expected, curve.evaluate(originalPowProductExpression));
    }
    
    @Test
    public void testPerformance() {
        int numIterations = 20;
        
        
        pln("==========================");
        pln(String.format("#iterations=%d, #bases=%d", numIterations, numBases));
        misc.tick();
        for (int i = 0; i < numIterations; i++) {
            curve.evaluate(originalPowProductExpression);
        }
        pln(String.format("normal pow -> %.2f ms", misc.tick()));
        misc.tick();
        
    }
}
