package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import jdk.nashorn.internal.objects.Global;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import static de.upb.crypto.math.swante.MyExponentiationAlgorithms.powUsingSlidingWindow;
import static de.upb.crypto.math.swante.MyExponentiationAlgorithms.precomputeSmallOddPowers;
import static de.upb.crypto.math.swante.misc.pln;

@RunWith(value = Parameterized.class)
public class EllipticCurvePointTests {
    
    private MyShortFormWeierstrassCurve curve;
    
    public EllipticCurvePointTests(MyShortFormWeierstrassCurve curve) {
        this.curve = curve;
    }
    
    @Test
    public void testCorrectness() {
        AbstractEllipticCurvePoint g = curve.generator;
        Assert.assertEquals(((Zp.ZpElement)g.add(g).normalize().getX()).getInteger(), new BigInteger("56515219790691171413109057904011688695424810155802929973526481321309856242040"));
        Assert.assertEquals(((Zp.ZpElement)((AbstractEllipticCurvePoint)g.pow(101)).normalize().getX()).getInteger(), new BigInteger("93980847734016439027508041847036757272229093243964019053297849828346202436527"));
    }
    
    @Test
    public void testSpeed() {
        pln("=========================");
        pln("Running performance tests for curve: " + curve.toString());
        AbstractEllipticCurvePoint g = curve.generator;
        int numIterations = 50000;
        int numPowerIterations = 200;
        misc.tick();
        AbstractEllipticCurvePoint tmp = g;
        for (int i = 0; i < numIterations; i++) {
            tmp = tmp.add(tmp);
        }
        tmp = tmp.normalize();
        double elapsed = misc.tick();
        pln(String.format("time for %d point doubles (and one final normalization): %.1f ms", numIterations, elapsed));
        misc.tick();
        tmp = g;
        for (int i = 0; i < numIterations; i++) {
            tmp = tmp.add(g);
        }
        tmp = tmp.normalize();
        elapsed = misc.tick();
        pln(String.format("time for %d point additions (and one final normalization): %.1f ms", numIterations, elapsed));
        tmp = g;
        Zp.ZpElement power = (Zp.ZpElement) g.getX();
        int windowSize = 3;
        int m = (1 << windowSize)-1;
        MyGlobals.useCurvePointNormalizationPowOptimization = false;
        misc.tick();
        for (int i = 0; i < numPowerIterations; i++) {
            GroupElement[] precomputedPowers = precomputeSmallOddPowers(tmp, m);
            tmp = (AbstractEllipticCurvePoint)powUsingSlidingWindow(tmp, power.getInteger(), windowSize, precomputedPowers);
            tmp = tmp.normalize();
        }
        elapsed = misc.tick();
        pln(String.format("time for %d pow G.x computations (and one normalization after each pow), without normalization optimization: %.1f ms", numPowerIterations, elapsed));
        MyGlobals.useCurvePointNormalizationPowOptimization = true;
        misc.tick();
        for (int i = 0; i < numPowerIterations; i++) {
            GroupElement[] precomputedPowers = precomputeSmallOddPowers(tmp, m);
            tmp = (AbstractEllipticCurvePoint)powUsingSlidingWindow(tmp, power.getInteger(), windowSize, precomputedPowers);
            tmp = tmp.normalize();
        }
        elapsed = misc.tick();
        pln(String.format("time for %d pow G.x computations (and one normalization after each pow), with normalization optimization: %.1f ms", numPowerIterations, elapsed));
    }
    
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<MyShortFormWeierstrassCurve> getParams() {
        ArrayList<MyShortFormWeierstrassCurve> list = new ArrayList<>();
        MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
//        list.add(new MyAffineCurve(parameters));
        list.add(new MyJacobiCurve(parameters));
        list.add(new MyProjectiveCurve(parameters));
        return list;
    }
    
}
