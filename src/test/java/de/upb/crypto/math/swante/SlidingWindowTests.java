package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class SlidingWindowTests {
    
    MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
    MyProjectiveCurve curve = new MyProjectiveCurve(parameters);
    
    @Test
    public void testCorrectness() {
        AbstractEllipticCurvePoint g = curve.getGenerator();
        GroupElement[] expected = {g.pow(1), g.pow(3), g.pow(5), g.pow(7)};
        GroupElement[] actual = g.precomputePowersForSlidingWindow(3);
        Assert.assertArrayEquals(expected,actual);
    }
}
