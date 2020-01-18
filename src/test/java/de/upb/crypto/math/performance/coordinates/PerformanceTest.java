package de.upb.crypto.math.performance.coordinates;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigProvider;
import de.upb.crypto.math.pairings.generic.PairingSourceGroup;
import de.upb.crypto.math.pairings.generic.PairingSourceGroupElement;
import de.upb.crypto.math.structures.ec.AffineECPCoordinate;
import de.upb.crypto.math.structures.ec.ProjectiveECPCoordinate;
import de.upb.crypto.math.structures.zn.Zn;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Coordinate representations have problem for performance tests in that we need to be sure to use the same
 * parameters to get an accurate comparison. This class, compared to
 * {@link de.upb.crypto.math.performance.pairing.PairingPerformanceTest}, makes sure that this is done correctly
 * by using the same group elements for both tests. When necessary, elements are converted between representations.
 */
public class PerformanceTest {

    private static BilinearMap affinePairing;
    private static BilinearMap projectivePairing;
    private static GroupElement[] affineBasisElements;
    private static GroupElement[] projectiveBasisElements;
    private static GroupElement affineG2Elem;
    private static GroupElement projectiveG2Elem;
    private static Zn.ZnElement[] exponents;

    @BeforeClass
    public static void setupTest() {
        BarretoNaehrigProvider bnProvider = new BarretoNaehrigProvider();
        affinePairing = bnProvider.provideBilinearGroupFromSpec(BarretoNaehrigProvider.ParamSpecs.SFC256,
                AffineECPCoordinate.class).getBilinearMap();
        projectivePairing = bnProvider.provideBilinearGroupFromSpec(BarretoNaehrigProvider.ParamSpecs.SFC256,
                ProjectiveECPCoordinate.class).getBilinearMap();

        int numElements = 50;
        affineBasisElements = new GroupElement[numElements];
        projectiveBasisElements = new GroupElement[numElements];
        exponents = new Zn.ZnElement[numElements];
        Zn zn = new Zn(affinePairing.getG1().size());
        for (int i = 0; i < numElements; ++i) {
            affineBasisElements[i] = (affinePairing.getG1().getUniformlyRandomNonNeutral());
            projectiveBasisElements[i] = affineToProjective(projectivePairing.getG1(), affineBasisElements[i]);
            exponents[i] = zn.getUniformlyRandomElement();
        }
        affineG2Elem = affinePairing.getG2().getUniformlyRandomNonNeutral();
        projectiveG2Elem = affineToProjective(projectivePairing.getG2(), affineG2Elem);
    }

    @Test
    public void testAffineOpPow() {
        System.out.println("Testing affine coordinate op pow:");
        GroupElement result = affineBasisElements[0].pow(exponents[0]);
        long referenceTime = System.nanoTime();
        for (int i = 1; i < affineBasisElements.length; ++i) {
            result.op(affineBasisElements[i].pow(exponents[i]));
        }
        System.out.println("Time to evaluate: " + (System.nanoTime() - referenceTime) / 1e6 + " ms");
        System.out.println("Result: " + result);
    }

    @Test
    public void testProjectiveOpPow() {
        System.out.println("Testing projective coordinate op pow:");
        // complicated way to turn first element into a projective coordinate
        GroupElement result = projectiveBasisElements[0].pow(exponents[0]);
        long referenceTime = System.nanoTime();
        for (int i = 1; i < projectiveBasisElements.length; ++i) {
            result.op(projectiveBasisElements[i].pow(exponents[i]));
        }
        System.out.println("Time to evaluate: " + (System.nanoTime() - referenceTime) / 1e6 + " ms");
        System.out.println("Result: " + ((PairingSourceGroupElement) result).normalize());
    }

    @Test
    public void testAffinePairing() {
        System.out.println("Testing affine coordinate pairing:");
        System.out.println("g1Elem: " + affineBasisElements[0]);
        System.out.println("g2Elem: " + affineG2Elem);
        long referenceTime = System.nanoTime();
        GroupElement result = affinePairing.apply(affineBasisElements[0], affineG2Elem);
        System.out.println("Time to evaluate: " + (System.nanoTime() - referenceTime) / 1e6 + " ms");
        System.out.println("Result: " + result);
    }

    @Test
    public void testProjectivePairing() {
        System.out.println("Testing projective coordinate pairing:");
        System.out.println("g1Elem: " + projectiveBasisElements[0]);
        System.out.println("g2Elem: " + projectiveG2Elem);
        long referenceTime = System.nanoTime();
        GroupElement result = affinePairing.apply(projectiveBasisElements[0], projectiveG2Elem);
        System.out.println("Time to evaluate: " + (System.nanoTime() - referenceTime) / 1e6 + " ms");
        System.out.println("Result: " + result);
    }

    private static PairingSourceGroupElement affineToProjective(Group g, GroupElement affineElem) {
        return ((PairingSourceGroup) g).getElement(((PairingSourceGroupElement) affineElem).getPoint().getNormalizedX(),
                        ((PairingSourceGroupElement) affineElem).getPoint().getNormalizedY());
    }
}
