package de.upb.crypto.math.curvepoints.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.structures.EllipticCurve;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigBilinearGroup;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigSourceGroupElement;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigTatePairing;
import de.upb.crypto.math.pairings.generic.PairingSourceGroupElement;
import de.upb.crypto.math.structures.ec.AffineECPCoordinate;
import de.upb.crypto.math.structures.ec.EllipticCurvePoint;
import de.upb.crypto.math.structures.ec.ProjectiveECPCoordinate;
import org.junit.Test;

public class ProjPairingTest {

    @Test
    public void testTangentLine() {
        BarretoNaehrigProvider bnFac = new BarretoNaehrigProvider();
        BarretoNaehrigBilinearGroup bnGroup = bnFac.provideBilinearGroup(128,
                new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3, true, true, false),
                ProjectiveECPCoordinate.class);

        for (int i = 0; i < 100; ++i) {
            PairingSourceGroupElement P = (PairingSourceGroupElement) bnGroup.getG1().getUniformlyRandomNonNeutral();
            PairingSourceGroupElement Q = (PairingSourceGroupElement) bnGroup.getG2().getUniformlyRandomNonNeutral();
            Q = (PairingSourceGroupElement) Q.normalize();
            BarretoNaehrigTatePairing pairing =
                    new BarretoNaehrigTatePairing(bnGroup.getG1(), bnGroup.getG2(), bnGroup.getGT());
            FieldElement[] projLine = P.computeLine(P);
            FieldElement projRes = pairing.evaluateLine(projLine, P, Q);

            PairingSourceGroupElement affineP = new BarretoNaehrigSourceGroupElement(
                    new AffineECPCoordinate(P.getStructure(), P.getNormalizedX(), P.getNormalizedY())
            );
            assert affineP.getPoint() instanceof AffineECPCoordinate;
            FieldElement[] affineLine = affineP.computeLine(affineP);
            FieldElement affineRes = pairing.evaluateLine(affineLine, affineP, Q);
            System.out.println("Proj result: " + projRes);
            System.out.println("Affine result: " + affineRes);
            assert projRes.equals(affineRes);
        }
    }

    @Test
    public void testPairing() {
        BarretoNaehrigProvider bnFac = new BarretoNaehrigProvider();
        BarretoNaehrigBilinearGroup bnGroup = bnFac.provideBilinearGroup(128,
                new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3, true, true, false),
                AffineECPCoordinate.class);
        PairingSourceGroupElement p1 = (PairingSourceGroupElement) bnGroup.getG1().getGenerator();
        PairingSourceGroupElement projP1 = new BarretoNaehrigSourceGroupElement(
                new ProjectiveECPCoordinate(p1.getStructure(), p1.getNormalizedX(), p1.getNormalizedY())
        );

        System.out.println(bnGroup.getBilinearMap().apply(p1, p1));
        System.out.println(bnGroup.getBilinearMap().apply(projP1, projP1));

    }
}
