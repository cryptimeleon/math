package de.upb.crypto.math.swante;

import de.upb.crypto.math.structures.ec.MyProjectiveEllipticCurvePoint;
import de.upb.crypto.math.structures.quotient.FiniteFieldExtension;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static de.upb.crypto.math.swante.misc.pln;




public class SimpleImplementationTests {
    
    MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
    
    @Test
    public void testCorrectness() {
        MyProjectiveTriple g = new MyProjectiveTriple(parameters.gx, parameters.gy);
        BigInteger p = parameters.p;
        BigInteger a = parameters.a;
        Assert.assertEquals(g.add(p,a,g).normalize(p).x, new BigInteger("56515219790691171413109057904011688695424810155802929973526481321309856242040"));
        Assert.assertEquals(g.pow(p,a,BigInteger.valueOf(101)).normalize(p).x, new BigInteger("93980847734016439027508041847036757272229093243964019053297849828346202436527"));
        Zp zp = new Zp(p);
        MyShortFormWeierstrassCurve curve = new MyProjectiveCurve(parameters);
        MyProjectiveEllipticCurvePoint x = new MyProjectiveEllipticCurvePoint(curve, zp.new ZpElement(g.x), zp.new ZpElement(g.y), zp.new ZpElement(g.z));
        pln(x);
        Assert.assertEquals(((Zp.ZpElement) ((MyProjectiveEllipticCurvePoint) x.square()).getX()).getInteger(), g.times2(p,a).x);
        Assert.assertEquals(((Zp.ZpElement) ((MyProjectiveEllipticCurvePoint) x.add(x).add(x)).getX()).getInteger(), g.add(p,a,g).add(p,a,g).x);
    }
    
    @Test
    public void testMySimpleProjectiveComputations() {
        pln("=========================");
        MyProjectiveTriple g = new MyProjectiveTriple(parameters.gx, parameters.gy);
        BigInteger p = parameters.p;
        BigInteger a = parameters.a;
        int numIterations = 500000;
        int numPowerIterations = 2000;
        misc.tick();
        MyProjectiveTriple tmp = g;
        for (int i = 0; i < numIterations; i++) {
            tmp = tmp.add(p, a, tmp);
        }
        tmp = tmp.normalize(p);
        double elapsed = misc.tick();
        pln(String.format("time for %d point doubles (and one final normalization): %.1f ms", numIterations, elapsed));
        misc.tick();
        tmp = g;
        for (int i = 0; i < numIterations; i++) {
            tmp = tmp.add(p,a, g);
        }
        tmp = tmp.normalize(p);
        elapsed = misc.tick();
        pln(String.format("time for %d point additions (and one final normalization): %.1f ms", numIterations, elapsed));
        tmp = g;
        BigInteger power = parameters.gx;
        misc.tick();
        for (int i = 0; i < numPowerIterations; i++) {
            tmp = tmp.pow(p, a, power);
            tmp = tmp.normalize(p);
        }
        elapsed = misc.tick();
        pln(String.format("time for %d pow G.x computations (and one normalization after each pow): %.1f ms", numPowerIterations, elapsed));
    }
}
