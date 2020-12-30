package de.upb.crypto.math.structures.test;

import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.counting.CountingBilinearGroup;
import de.upb.crypto.math.pairings.counting.CountingGroup;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CountingTest {

    CountingBilinearGroup bilGroup;

    @Before
    public void setUp() {
        bilGroup = new CountingBilinearGroup(128, BilinearGroup.Type.TYPE_2);
    }

    @Test
    public void testNullInstantiation() {
        CountingGroup countingGroup = (CountingGroup) bilGroup.getG1();
        assertEquals(0, countingGroup.getNumRetrievedRepresentations());
        assertEquals(0, countingGroup.getNumExps());
        assertEquals(0, countingGroup.getNumOpsTotal());
        assertEquals(0, countingGroup.getNumSquaringsTotal());
        assertEquals(0, countingGroup.getNumInversionsTotal());
        assertEquals(0, countingGroup.getNumOpsNoExpMultiExp());
        assertEquals(0, countingGroup.getNumSquaringsNoExpMultiExp());
        assertEquals(0, countingGroup.getNumInversionsNoExpMultiExp());
        assertArrayEquals(new Integer[] {}, countingGroup.getMultiExpTermNumbers().toArray(new Integer[] {}));
    }

    @Test
    public void testBasicOperationCounting() {
        CountingGroup countingGroup = (CountingGroup) bilGroup.getG1();
        GroupElement elem = countingGroup.getUniformlyRandomNonNeutral();
        GroupElement elem2 = countingGroup.getUniformlyRandomNonNeutral();
        elem.op(elem).inv().computeSync();
        elem.op(elem2).computeSync();

        assertEquals(1, countingGroup.getNumOpsNoExpMultiExp());
        assertEquals(1, countingGroup.getNumOpsTotal());
        assertEquals(1, countingGroup.getNumInversionsNoExpMultiExp());
        assertEquals(1, countingGroup.getNumInversionsTotal());
        assertEquals(1, countingGroup.getNumSquaringsNoExpMultiExp());
        assertEquals(1, countingGroup.getNumSquaringsTotal());
    }

    @Test
    public void testPowCounting() {
        CountingGroup countingGroup = (CountingGroup) bilGroup.getG1();
        GroupElement elem = countingGroup.getUniformlyRandomNonNeutral();
        elem.pow(10).computeSync();
        // tested with WNAF exponentiation algorithm
        assertEquals(8, countingGroup.getNumOpsTotal());
        assertEquals(2, countingGroup.getNumSquaringsTotal());
        assertEquals(1, countingGroup.getNumExps());
    }

    @Test
    public void testRepresentationCounting() {
        CountingGroup countingGroup = (CountingGroup) bilGroup.getG1();
        GroupElement elem = countingGroup.getUniformlyRandomNonNeutral();
        elem.getRepresentation();
        elem.getRepresentation();
        assertEquals(2, countingGroup.getNumRetrievedRepresentations());
    }

    @Test
    public void testPairingCounting() {
        CountingGroup groupG1 = (CountingGroup) bilGroup.getG1();
        CountingGroup groupG2 = (CountingGroup) bilGroup.getG2();
        CountingGroup groupGT = (CountingGroup) bilGroup.getGT();
        GroupElement elemG1 = groupG1.getUniformlyRandomNonNeutral();
        GroupElement elemG2 = groupG2.getUniformlyRandomNonNeutral();
        bilGroup.getBilinearMap().apply(elemG1, elemG2).computeSync();
        assertEquals(1, bilGroup.getNumPairings());
        // don't do unnecessary exponentiations in any of the groups
        assertEquals(0, groupG1.getNumExps());
        assertEquals(0, groupG2.getNumExps());
        assertEquals(0, groupGT.getNumExps());

        bilGroup.getBilinearMap().apply(elemG1, elemG2, BigInteger.TEN).computeSync();
        assertEquals(2, bilGroup.getNumPairings());
        // exp is done in GT currently (tested with WNAF exponentiation algorithm)
        assertEquals(8, groupGT.getNumOpsTotal());
        assertEquals(2, groupGT.getNumSquaringsTotal());
        assertEquals(1, groupGT.getNumExps());
    }

    @Test
    public void testMultiExpCounting() {
        CountingGroup countingGroup = (CountingGroup) bilGroup.getG1();
        GroupElement elem1, elem2, elem3;
        do {
            elem1 = countingGroup.getUniformlyRandomNonNeutral();
            elem2 = countingGroup.getUniformlyRandomNonNeutral();
            elem3 = countingGroup.getUniformlyRandomNonNeutral();
            // Cannot equal each other, else the operations will be different since we don't
            // need to do as many precomputations
        } while (elem1.equals(elem2) || elem2.equals(elem3) || elem3.equals(elem1));

        // will be computed as (G1_5 * G2_5 * G3_5)^2 where Gi_5 = elemi^5 is precomputed (WNAF multiexp)
        elem1.pow(10).op(elem2.pow(10)).op(elem3.pow(10)).computeSync();
        System.out.println(countingGroup.formatCounterData());
        assertArrayEquals(new Integer[] {3}, countingGroup.getMultiExpTermNumbers().toArray(new Integer[1]));
        // 8 ops per element for precomputation
        assertEquals(24, countingGroup.getNumOpsTotal());
        // 1 squaring per element for precomputation plus one for multi-exponentiation
        assertEquals(4, countingGroup.getNumSquaringsTotal());
    }

    @Test
    public void testComputeWorksSynchronously() {
        CountingGroup countingGroup = (CountingGroup) bilGroup.getG1();
        GroupElement elem = countingGroup.getUniformlyRandomNonNeutral();
        elem.op(elem).inv().compute(); // compute is implemented synchronously for the counting group
        assertEquals(1, countingGroup.getNumSquaringsNoExpMultiExp()); // would be 0 if asynchronous
    }

    @Test
    public void testResetCounters() {
        CountingGroup groupG1 = (CountingGroup) bilGroup.getG1();
        CountingGroup groupG2 = (CountingGroup) bilGroup.getG2();
        GroupElement elem1 = groupG1.getUniformlyRandomNonNeutral();
        GroupElement elem2 = groupG1.getUniformlyRandomNonNeutral();
        GroupElement elem3 = groupG1.getUniformlyRandomNonNeutral();

        elem1.pow(10).op(elem2.pow(10)).op(elem3.pow(10)).computeSync();

        GroupElement elemG1 = groupG1.getUniformlyRandomNonNeutral();
        GroupElement elemG2 = groupG2.getUniformlyRandomNonNeutral();
        bilGroup.getBilinearMap().apply(elemG1, elemG2, BigInteger.TEN).computeSync();

        elem1.op(elem2).inv().computeSync();

        elem1.getRepresentation();

        bilGroup.resetCounters();

        assertEquals(0, groupG1.getNumRetrievedRepresentations());
        assertEquals(0, groupG1.getNumExps());
        assertEquals(0, groupG1.getNumOpsTotal());
        assertEquals(0, groupG1.getNumSquaringsTotal());
        assertEquals(0, groupG1.getNumInversionsTotal());
        assertEquals(0, groupG1.getNumOpsNoExpMultiExp());
        assertEquals(0, groupG1.getNumSquaringsNoExpMultiExp());
        assertEquals(0, groupG1.getNumInversionsNoExpMultiExp());
        assertArrayEquals(new Integer[] {}, groupG1.getMultiExpTermNumbers().toArray(new Integer[] {}));

        assertEquals(0, bilGroup.getNumPairings());
    }

    @Test
    public void testCountingHomomorphism() {
        CountingGroup groupG1 = (CountingGroup) bilGroup.getG1();
        CountingGroup groupG2 = (CountingGroup) bilGroup.getG2();

        GroupElement elemG1;
        GroupElement elemG2 = groupG2.getUniformlyRandomNonNeutral();

        elemG1 = bilGroup.getHomomorphismG2toG1().apply(elemG2);
        assertEquals(elemG1.getStructure(), groupG1);
    }
}
