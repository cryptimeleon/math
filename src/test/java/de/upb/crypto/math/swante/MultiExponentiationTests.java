package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;
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
        PowProductExpression powProductExpression = new PowProductExpression(curve);
        for (int i = 0; i < numBases; i++) {
            AbstractEllipticCurvePoint base = bases.get(i);
            BigInteger e = exponents.get(i);
            expected = expected.op(base.pow(e));
            powProductExpression.op(base, e);
        }
        Assert.assertEquals(expected, curve.evaluate(powProductExpression));
    }
    
    @Test
    public void testPerformance() {
        int numIterations = 10;
        
        
        pln("==========================");
//        pln(String.format("wsize=%d (m=%d), #bases=%d, #exponents=%d", windowSize, m, numBases, numExponents));
        misc.tick();
        for (int i = 0; i < numIterations; i++) {
            AbstractEllipticCurvePoint base = bases.get(i);
            
        }
        pln(String.format("normal pow -> %.2f ms", misc.tick()));
        misc.tick();
        
    }
}
