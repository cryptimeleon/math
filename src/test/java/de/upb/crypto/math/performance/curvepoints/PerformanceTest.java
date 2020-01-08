package de.upb.crypto.math.performance.curvepoints;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.generic.PairingSourceGroup;
import de.upb.crypto.math.pairings.generic.PairingSourceGroupElement;
import de.upb.crypto.math.structures.ec.AffineECPCoordinate;
import de.upb.crypto.math.structures.ec.ProjectiveECPCoordinate;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Coordinate representations have problem for performance tests that we need to be sure to use the same
 * parameters to get an accurate comparison. This class, compared to
 * {@link de.upb.crypto.math.performance.pairing.PairingPerformanceTest}, makes sure that this is done correctly
 * by using the same group elements for both tests.
 */
public class PerformanceTest {

    private static BilinearMap affinePairing;
    private static BilinearMap projectivePairing;
    private static GroupElement[] basisElements;
    private static Zn.ZnElement[] exponents;

    @BeforeClass
    public static void setupTest() {
        BarretoNaehrigProvider bnProvider = new BarretoNaehrigProvider();
        affinePairing = bnProvider.provideBilinearGroupFromSpec(BarretoNaehrigProvider.ParamSpecs.SFC256,
                AffineECPCoordinate.class).getBilinearMap();
        projectivePairing = bnProvider.provideBilinearGroupFromSpec(BarretoNaehrigProvider.ParamSpecs.SFC256,
                ProjectiveECPCoordinate.class).getBilinearMap();

        int numElements = 50;
        basisElements = new GroupElement[numElements];
        exponents = new Zn.ZnElement[numElements];
        Zn zn = new Zn(affinePairing.getG1().size());
        for (int i = 0; i < numElements; ++i) {
            basisElements[i] = (affinePairing.getG1().getUniformlyRandomNonNeutral());
            exponents[i] = zn.getUniformlyRandomElement();
        }
    }

    @Test
    public void testAffine() {
        System.out.println("Testing affine coordinate:");
        GroupElement result = basisElements[0].pow(exponents[0]);
        long referenceTime = System.nanoTime();
        for (int i = 1; i < basisElements.length; ++i) {
            result.op(basisElements[i].pow(exponents[i]));
        }
        System.out.println("Time to evaluate: " + (System.nanoTime() - referenceTime) / 1e6 + " ms");
        System.out.println(((PairingSourceGroupElement) result).getPoint().getClass());
        System.out.println("Result: " + result);
    }

    @Test
    public void testProjective() {
        System.out.println("Testing projective coordinate:");
        // complicated way to turn first element into a projective coordinate
        GroupElement result = ((PairingSourceGroup) projectivePairing.getG1())
                .getElement(((PairingSourceGroupElement) basisElements[0]).getPoint().getNormalizedX(),
                        ((PairingSourceGroupElement) basisElements[0]).getPoint().getNormalizedY()).pow(exponents[0]);
        long referenceTime = System.nanoTime();
        for (int i = 1; i < basisElements.length; ++i) {
            result.op(basisElements[i].pow(exponents[i]));
        }
        System.out.println("Time to evaluate: " + (System.nanoTime() - referenceTime) / 1e6 + " ms");
        System.out.println(((PairingSourceGroupElement) result).getPoint().getClass());
        System.out.println("Result: " + ((PairingSourceGroupElement) result).normalize());
    }
}
