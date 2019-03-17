package de.upb.crypto.math.swante;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

import static de.upb.crypto.math.swante.misc.pln;




public class MyTests {
    
    MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
    
    @Test
    public void testCorrectness() {
        mymath.ProjectiveTriple g = new mymath.ProjectiveTriple(parameters.gx, parameters.gy);
        BigInteger p = parameters.p;
        BigInteger a = parameters.a;
        Assert.assertEquals(g.add(p,a,g).normalize(p).x, new BigInteger("56515219790691171413109057904011688695424810155802929973526481321309856242040"));
        Assert.assertEquals(g.pow(p,a,BigInteger.valueOf(101)).normalize(p).x, new BigInteger("93980847734016439027508041847036757272229093243964019053297849828346202436527"));
    }
    
    @Test
    public void testMySimpleProjectiveComputations() {
        pln("=========================");
        mymath.ProjectiveTriple g = new mymath.ProjectiveTriple(parameters.gx, parameters.gy);
        BigInteger p = parameters.p;
        BigInteger a = parameters.a;
        int numIterations = 100000;
        misc.tick();
        mymath.ProjectiveTriple tmp = g;
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
        int numPowerIterations = 1000;
        for (int i = 0; i < numPowerIterations; i++) {
            tmp = tmp.pow(p, a, power);
            tmp = tmp.normalize(p);
        }
        elapsed = misc.tick();
        pln(String.format("time for %d pow G.x computations (and one normalization after each pow): %.1f ms", numPowerIterations, elapsed));
    }
}
