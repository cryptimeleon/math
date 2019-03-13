package de.upb.crypto.math.swante;

import com.google.common.base.Strings;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.PairingSourceGroup;
import de.upb.crypto.math.structures.ec.*;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.Foo;
import de.upb.crypto.math.swante.Fooa;
import de.upb.crypto.math.swante.Foob;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

import static de.upb.crypto.math.swante.misc.pln;

@RunWith(value = Parameterized.class)
public class EllipticCurvePointTests {
    
    private MyShortFormWeierstrassCurve curve;
    
    public EllipticCurvePointTests(MyShortFormWeierstrassCurve curve) {
        this.curve = curve;
    }
    
    @Test
    public void testCorrectness() {
        MyAbstractEllipticCurvePoint g = curve.generator;
        Assert.assertEquals(g.add(g).normalize().x.getInteger(), new BigInteger("56515219790691171413109057904011688695424810155802929973526481321309856242040"));
        Assert.assertEquals(((MyAbstractEllipticCurvePoint)g.pow(101)).normalize().x.getInteger(), new BigInteger("93980847734016439027508041847036757272229093243964019053297849828346202436527"));
    }
    
    @Test
    public void testSpeed() {
        MyAbstractEllipticCurvePoint g = curve.generator;
        int numIterations = 10000;
        misc.tick();
        MyAbstractEllipticCurvePoint tmp = g;
        for (int i = 0; i < numIterations; i++) {
            tmp = tmp.add(tmp);
        }
        tmp = tmp.normalize();
        double elapsed = misc.tick();
        pln(String.format("time for %d point doubles: %.1f ms", numIterations, elapsed));
        misc.tick();
        tmp = g;
        for (int i = 0; i < numIterations; i++) {
            tmp = tmp.add(g);
        }
        tmp = tmp.normalize();
        elapsed = misc.tick();
        pln(String.format("time for %d point additions: %.1f ms", numIterations, elapsed));
        tmp = g;
        Zp.ZpElement power = g.x;
        misc.tick();
        int numPowerIterations = 100;
        for (int i = 0; i < numPowerIterations; i++) {
            tmp = (MyAbstractEllipticCurvePoint)tmp.pow(power);
        }
        tmp = tmp.normalize();
        elapsed = misc.tick();
        pln(String.format("time for %d pow G.x computations: %.1f ms", numPowerIterations, elapsed));
    }
    
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<MyShortFormWeierstrassCurve> getParams() {
        ArrayList<MyShortFormWeierstrassCurve> list = new ArrayList<>();
        MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
        list.add(new MyAffineCurve(parameters));
        list.add(new MyProjectiveCurve(parameters));
        return list;
    }
    
}
