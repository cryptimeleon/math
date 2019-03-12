package de.upb.crypto.math.swante;

import com.google.common.base.Strings;
import de.upb.crypto.math.pairings.generic.PairingSourceGroup;
import de.upb.crypto.math.structures.ec.*;
import de.upb.crypto.math.swante.Foo;
import de.upb.crypto.math.swante.Fooa;
import de.upb.crypto.math.swante.Foob;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
    
    }
    
    @Test
    public void testSpeed() {
        double start = misc.tick();
        double end = misc.tick();
        pln(String.format("elapsed time: %.1f ms", end-start));
    }
    
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<MyShortFormWeierstrassCurve> getParams() {
        ArrayList<MyShortFormWeierstrassCurve> list = new ArrayList<>();
        MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
        list.add(new MyAffineCurve(parameters));
        return list;
    }
    
}
