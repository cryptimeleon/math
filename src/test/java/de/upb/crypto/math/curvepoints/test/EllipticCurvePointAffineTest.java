package de.upb.crypto.math.curvepoints.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigBilinearGroup;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.pairings.supersingular.SupersingularProvider;
import de.upb.crypto.math.pairings.supersingular.SupersingularTateGroup;
import de.upb.crypto.math.structures.ec.AbstractECPCoordinate;
import de.upb.crypto.math.structures.ec.AffineECPCoordinate;
import de.upb.crypto.math.structures.ec.EllipticCurvePoint;
import de.upb.crypto.math.structures.ec.ProjectiveECPCoordinate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

@RunWith(Parameterized.class)
public class EllipticCurvePointAffineTest {

    private BilinearGroup bilGroup;

    public EllipticCurvePointAffineTest(BilinearGroup bilGroup) {
        this.bilGroup = bilGroup;
    }

    @Test
    public void testOp() {
        // Test G1
        EllipticCurvePoint neutralElement = (EllipticCurvePoint) bilGroup.getG1().getNeutralElement();
        assert(neutralElement.isNeutralElement());
        EllipticCurvePoint randomElement = (EllipticCurvePoint) bilGroup.getG1().getUniformlyRandomNonNeutral();
        // elements start off normalized
        assert(randomElement.isNormalized());
        EllipticCurvePoint randomElementTwice = randomElement.add(randomElement);
        // affine elements stay normalized
        assert(randomElementTwice.isNormalized());
        EllipticCurvePoint randomElementTwiceLine = randomElement.add(randomElement, randomElement.computeLine(randomElement));
        assert (randomElementTwiceLine.isNormalized());
        // adding with and without line has same result
        assert(randomElementTwice.equals(randomElementTwiceLine));
    }

    @Parameterized.Parameters(name = "Test: {0}")
    public static Collection<BilinearGroup> data() {
        List<BilinearGroup> bilinearGroups = new LinkedList<>();
        // BN curves
        BarretoNaehrigProvider bnFac = new BarretoNaehrigProvider();
        BarretoNaehrigBilinearGroup bnGroup = bnFac.provideBilinearGroup(128,
                new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3, true, true, false),
                AffineECPCoordinate.class);
        bilinearGroups.add(bnGroup);
        // Supersingular curves
        SupersingularProvider supsingFac = new SupersingularProvider();
        SupersingularTateGroup supsingGroup = supsingFac.provideBilinearGroup(80,
                new BilinearGroupRequirement(BilinearGroup.Type.TYPE_1, true, true, false),
                AffineECPCoordinate.class);
        bilinearGroups.add(supsingGroup);

        return bilinearGroups;
    }
}
