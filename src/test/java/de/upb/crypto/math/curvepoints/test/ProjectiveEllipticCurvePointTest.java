package de.upb.crypto.math.curvepoints.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.EllipticCurve;
import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigSourceGroup;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import org.junit.Test;

public class ProjectiveEllipticCurvePointTest {

    BarretoNaehrigSourceGroup curve;

    public ProjectiveEllipticCurvePointTest() {
        // get a curve for testing
        BilinearGroupFactory fac = new BilinearGroupFactory(12);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup group = fac.createBilinearGroup();
        curve = (BarretoNaehrigSourceGroup) group.getG1();
    }

    @Test
    public testAddCorrect() {
        EllipticCurvePoint P = curve.getUniformlyRandomElement();
        System.out.println("P: " + P);
        EllipticCurvePoint Q = curve.getUniformlyRandomElement();
        System.out.println("Q: " + Q);
        // Need beetter equals probably here
        assert()
    }
}
