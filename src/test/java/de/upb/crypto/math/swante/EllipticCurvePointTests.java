package de.upb.crypto.math.swante;

import de.upb.crypto.math.pairings.generic.PairingSourceGroup;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.ec.AffineEllipticCurvePoint;
import de.upb.crypto.math.swante.Foo;
import de.upb.crypto.math.swante.Fooa;
import de.upb.crypto.math.swante.Foob;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class EllipticCurvePointTests {
    
    @Test
    public void testA() {
        Assert.assertFalse(true)
    }
    
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<AbstractEllipticCurvePoint> getParams() {
        ArrayList<AbstractEllipticCurvePoint> list = new ArrayList<>();
        PairingSourceGroup pairingSourceGroup = new PairingSourceGroup();
        list.add(new AffineEllipticCurvePoint(pairingSourceGroup));
        return list;
    }
}
