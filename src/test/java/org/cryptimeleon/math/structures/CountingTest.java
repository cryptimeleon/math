package org.cryptimeleon.math.structures;

import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.debug.DebugBilinearGroup;;
import org.cryptimeleon.math.structures.groups.debug.DebugGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountingTest {

    DebugBilinearGroup bilGroup;

    @Before
    public void setUp() {
        bilGroup = new DebugBilinearGroup(128, BilinearGroup.Type.TYPE_2);
    }

    @Test
    public void testNullInstantiation() {
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        assertEquals(0, debugGroup.getNumRetrievedRepresentations());
        assertEquals(0, debugGroup.getNumExps());
        assertEquals(0, debugGroup.getNumOpsTotal());
        assertEquals(0, debugGroup.getNumSquaringsTotal());
        assertEquals(0, debugGroup.getNumInversionsTotal());
        assertEquals(0, debugGroup.getNumOpsNoExpMultiExp());
        assertEquals(0, debugGroup.getNumSquaringsNoExpMultiExp());
        assertEquals(0, debugGroup.getNumInversionsNoExpMultiExp());
        assertArrayEquals(new Integer[] {}, debugGroup.getMultiExpTermNumbers().toArray(new Integer[] {}));
    }

    @Test
    public void testBasicOperationCounting() {
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        GroupElement elem = debugGroup.getUniformlyRandomNonNeutral();
        GroupElement elem2 = debugGroup.getUniformlyRandomNonNeutral();
        elem.op(elem).inv().computeSync();
        elem.op(elem2).computeSync();

        assertEquals(1, debugGroup.getNumOpsNoExpMultiExp());
        assertEquals(1, debugGroup.getNumOpsTotal());
        assertEquals(1, debugGroup.getNumInversionsNoExpMultiExp());
        assertEquals(1, debugGroup.getNumInversionsTotal());
        assertEquals(1, debugGroup.getNumSquaringsNoExpMultiExp());
        assertEquals(1, debugGroup.getNumSquaringsTotal());
    }

    @Test
    public void testPowCounting() {
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        GroupElement elem = debugGroup.getUniformlyRandomNonNeutral();
        elem.pow(10).computeSync();
        // tested with WNAF exponentiation algorithm
        assertEquals(8, debugGroup.getNumOpsTotal());
        assertEquals(2, debugGroup.getNumSquaringsTotal());
        assertEquals(1, debugGroup.getNumExps());
    }

    @Test
    public void testRepresentationCounting() {
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        GroupElement elem = debugGroup.getUniformlyRandomNonNeutral();
        elem.getRepresentation();
        elem.getRepresentation();
        assertEquals(2, debugGroup.getNumRetrievedRepresentations());
    }

    @Test
    public void testPairingCounting() {
        DebugGroup groupG1 = (DebugGroup) bilGroup.getG1();
        DebugGroup groupG2 = (DebugGroup) bilGroup.getG2();
        DebugGroup groupGT = (DebugGroup) bilGroup.getGT();
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
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        GroupElement elem1, elem2, elem3;
        do {
            elem1 = debugGroup.getUniformlyRandomNonNeutral();
            elem2 = debugGroup.getUniformlyRandomNonNeutral();
            elem3 = debugGroup.getUniformlyRandomNonNeutral();
            // Cannot equal each other, else the operations will be different since we don't
            // need to do as many precomputations
        } while (elem1.equals(elem2) || elem2.equals(elem3) || elem3.equals(elem1));

        // will be computed as (G1_5 * G2_5 * G3_5)^2 where Gi_5 = elemi^5 is precomputed (WNAF multiexp)
        elem1.pow(10).op(elem2.pow(10)).op(elem3.pow(10)).computeSync();
        System.out.println(debugGroup.formatCounterData());
        assertArrayEquals(new Integer[] {3}, debugGroup.getMultiExpTermNumbers().toArray(new Integer[1]));
        // 8 ops per element for precomputation
        assertEquals(24, debugGroup.getNumOpsTotal());
        // 1 squaring per element for precomputation plus one for multi-exponentiation
        assertEquals(4, debugGroup.getNumSquaringsTotal());
    }

    @Test
    public void testComputeWorksSynchronously() {
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        GroupElement elem = debugGroup.getUniformlyRandomNonNeutral();
        elem.op(elem).inv().compute(); // compute is implemented synchronously for the counting group
        assertEquals(1, debugGroup.getNumSquaringsNoExpMultiExp()); // would be 0 if asynchronous
    }

    @Test
    public void testResetCounters() {
        DebugGroup groupG1 = (DebugGroup) bilGroup.getG1();
        DebugGroup groupG2 = (DebugGroup) bilGroup.getG2();
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
        DebugGroup groupG1 = (DebugGroup) bilGroup.getG1();
        DebugGroup groupG2 = (DebugGroup) bilGroup.getG2();

        GroupElement elemG1;
        GroupElement elemG2 = groupG2.getUniformlyRandomNonNeutral();

        elemG1 = bilGroup.getHomomorphismG2toG1().apply(elemG2);
        assertEquals(elemG1.getStructure(), groupG1);
    }
}
