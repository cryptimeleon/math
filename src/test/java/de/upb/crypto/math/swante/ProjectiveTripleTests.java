package de.upb.crypto.math.swante;

import de.upb.crypto.math.structures.ec.MyProjectiveEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.util.MyShortFormWeierstrassCurveParameters;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static de.upb.crypto.math.swante.util.MyUtil.pln;


/**
 * Tests for the correctness of the MyProjectiveTriple class
 */
public class ProjectiveTripleTests {
    
    MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
    
    @Test
    public void testCorrectness() {
        BigInteger p = parameters.p;
        BigInteger a = parameters.a;
        MyProjectiveTriple g = new MyProjectiveTriple(p, a, parameters.gx, parameters.gy);
        Assert.assertEquals(g.add(g).normalize(p).x, new BigInteger("56515219790691171413109057904011688695424810155802929973526481321309856242040"));
        Assert.assertEquals(g.pow(BigInteger.valueOf(101)).normalize(p).x, new BigInteger("93980847734016439027508041847036757272229093243964019053297849828346202436527"));
        Zp zp = new Zp(p);
        MyShortFormWeierstrassCurve curve = new MyProjectiveCurve(parameters);
        MyProjectiveEllipticCurvePoint x = new MyProjectiveEllipticCurvePoint(curve, zp.new ZpElement(g.x), zp.new ZpElement(g.y), zp.new ZpElement(g.z));
        pln(x);
        Assert.assertEquals(((Zp.ZpElement) ((MyProjectiveEllipticCurvePoint) x.square()).getX()).getInteger(), g.times2().x);
        Assert.assertEquals(((Zp.ZpElement) ((MyProjectiveEllipticCurvePoint) x.add(x).add(x)).getX()).getInteger(), g.add(g).add(g).x);
        Assert.assertEquals(((Zp.ZpElement) ((MyProjectiveEllipticCurvePoint) x.pow(11)).getX()).getInteger(), g.pow(BigInteger.valueOf(11)).x);
    }
    
}
