package org.cryptimeleon.math.structures;

import org.cryptimeleon.math.random.RandomGenerator;
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
        bilGroup = new DebugBilinearGroup(RandomGenerator.getRandomPrime(128), BilinearGroup.Type.TYPE_2);
    }

    @Test
    public void testNullInstantiation() {
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        String bucketName = getClass().getName() + "#testNullInstantiation";
        bilGroup.setBucket(bucketName);
        assertEquals(0, debugGroup.getNumRetrievedRepresentations(bucketName));
        assertEquals(0, debugGroup.getNumExps(bucketName));
        assertEquals(0, debugGroup.getNumOpsTotal(bucketName));
        assertEquals(0, debugGroup.getNumSquaringsTotal(bucketName));
        assertEquals(0, debugGroup.getNumInversionsTotal(bucketName));
        assertEquals(0, debugGroup.getNumOpsNoExpMultiExp(bucketName));
        assertEquals(0, debugGroup.getNumSquaringsNoExpMultiExp(bucketName));
        assertEquals(0, debugGroup.getNumInversionsNoExpMultiExp(bucketName));
        assertArrayEquals(new Integer[] {}, debugGroup.getMultiExpTermNumbers(bucketName).toArray(new Integer[] {}));
    }

    @Test
    public void testBasicOperationCounting() {
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        String bucketName = getClass().getName() + "#testBasicOperationCounting";
        bilGroup.setBucket(bucketName);
        GroupElement elem = debugGroup.getUniformlyRandomNonNeutral();
        GroupElement elem2 = debugGroup.getUniformlyRandomNonNeutral();
        elem.op(elem).inv().computeSync();
        elem.op(elem2).computeSync();

        assertEquals(1, debugGroup.getNumOpsNoExpMultiExp(bucketName));
        assertEquals(1, debugGroup.getNumOpsTotal(bucketName));
        assertEquals(1, debugGroup.getNumInversionsNoExpMultiExp(bucketName));
        assertEquals(1, debugGroup.getNumInversionsTotal(bucketName));
        assertEquals(1, debugGroup.getNumSquaringsNoExpMultiExp(bucketName));
        assertEquals(1, debugGroup.getNumSquaringsTotal(bucketName));
    }

    @Test
    public void testPowCounting() {
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        String bucketName = getClass().getName() +"#testPowCounting";
        bilGroup.setBucket(bucketName);
        GroupElement elem = debugGroup.getUniformlyRandomNonNeutral();
        elem.pow(10).computeSync();
        // tested with WNAF exponentiation algorithm
        assertEquals(8, debugGroup.getNumOpsTotal(bucketName));
        assertEquals(2, debugGroup.getNumSquaringsTotal(bucketName));
        assertEquals(1, debugGroup.getNumExps(bucketName));
    }

    @Test
    public void testRepresentationCounting() {
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        String bucketName = getClass().getName() + "#testRepresentationCounting";
        bilGroup.setBucket(bucketName);
        GroupElement elem = debugGroup.getUniformlyRandomNonNeutral();
        elem.getRepresentation();
        elem.getRepresentation();
        assertEquals(2, debugGroup.getNumRetrievedRepresentations(bucketName));
    }

    @Test
    public void testPairingCounting() {
        String bucketName = getClass().getName() + "#testPairingCounting";
        bilGroup.setBucket(bucketName);
        DebugGroup groupG1 = (DebugGroup) bilGroup.getG1();
        DebugGroup groupG2 = (DebugGroup) bilGroup.getG2();
        DebugGroup groupGT = (DebugGroup) bilGroup.getGT();
        GroupElement elemG1 = groupG1.getUniformlyRandomNonNeutral();
        GroupElement elemG2 = groupG2.getUniformlyRandomNonNeutral();
        bilGroup.getBilinearMap().apply(elemG1, elemG2).computeSync();
        assertEquals(1, bilGroup.getNumPairings(bucketName));
        // don't do unnecessary exponentiations in any of the groups
        assertEquals(0, groupG1.getNumExps(bucketName));
        assertEquals(0, groupG2.getNumExps(bucketName));
        assertEquals(0, groupGT.getNumExps(bucketName));

        bilGroup.getBilinearMap().apply(elemG1, elemG2, BigInteger.TEN).computeSync();
        assertEquals(2, bilGroup.getNumPairings(bucketName));
        // exp is done in GT currently (tested with WNAF exponentiation algorithm)
        assertEquals(8, groupGT.getNumOpsTotal(bucketName));
        assertEquals(2, groupGT.getNumSquaringsTotal(bucketName));
        assertEquals(1, groupGT.getNumExps(bucketName));
    }

    @Test
    public void testMultiExpCounting() {
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        String bucketName = getClass().getName() + "#testMultiExpCounting";
        bilGroup.setBucket(bucketName);
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
        assertArrayEquals(new Integer[] {3}, debugGroup.getMultiExpTermNumbers(bucketName).toArray(new Integer[1]));
        // 8 ops per element for precomputation
        assertEquals(24, debugGroup.getNumOpsTotal(bucketName));
        // 1 squaring per element for precomputation plus one for multi-exponentiation
        assertEquals(4, debugGroup.getNumSquaringsTotal(bucketName));
    }

    @Test
    public void testComputeWorksSynchronously() {
        String bucketName = getClass().getName() + "#testComputeWorksSynchronously";
        bilGroup.setBucket(bucketName);
        DebugGroup debugGroup = (DebugGroup) bilGroup.getG1();
        GroupElement elem = debugGroup.getUniformlyRandomNonNeutral();
        elem.op(elem).inv().compute(); // compute is implemented synchronously for the counting group
        assertEquals(1, debugGroup.getNumSquaringsNoExpMultiExp(bucketName)); // would be 0 if asynchronous
    }

    @Test
    public void testResetCounters() {
        String bucketName = getClass().getName() + "#testResetCounters";
        bilGroup.setBucket(bucketName);
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

        bilGroup.resetCounters(bucketName);

        assertEquals(0, groupG1.getNumRetrievedRepresentations(bucketName));
        assertEquals(0, groupG1.getNumExps(bucketName));
        assertEquals(0, groupG1.getNumOpsTotal(bucketName));
        assertEquals(0, groupG1.getNumSquaringsTotal(bucketName));
        assertEquals(0, groupG1.getNumInversionsTotal(bucketName));
        assertEquals(0, groupG1.getNumOpsNoExpMultiExp(bucketName));
        assertEquals(0, groupG1.getNumSquaringsNoExpMultiExp(bucketName));
        assertEquals(0, groupG1.getNumInversionsNoExpMultiExp(bucketName));
        assertArrayEquals(new Integer[] {}, groupG1.getMultiExpTermNumbers(bucketName).toArray(new Integer[] {}));

        assertEquals(0, bilGroup.getNumPairings(bucketName));
    }

    @Test
    public void testCountingHomomorphism() {
        String bucketName = getClass().getName() + "#testCountingHomomorphism";
        bilGroup.setBucket(bucketName);
        DebugGroup groupG1 = (DebugGroup) bilGroup.getG1();
        DebugGroup groupG2 = (DebugGroup) bilGroup.getG2();

        GroupElement elemG1;
        GroupElement elemG2 = groupG2.getUniformlyRandomNonNeutral();

        elemG1 = bilGroup.getHomomorphismG2toG1().apply(elemG2);
        assertEquals(elemG1.getStructure(), groupG1);
    }

    @Test
    public void testAllBuckets() {
        bilGroup.resetCountersAllBuckets();
        String bucketName1 = getClass().getName() + "#testAllBuckets(1)";
        String bucketName2 = getClass().getName() + "#testAllBuckets(2)";
        DebugGroup groupG1 = (DebugGroup) bilGroup.getG1();
        DebugGroup groupG2 = (DebugGroup) bilGroup.getG2();
        DebugGroup groupGT = (DebugGroup) bilGroup.getGT();

        bilGroup.setBucket(bucketName1);
        GroupElement elem1 = groupG1.getUniformlyRandomNonNeutral();
        GroupElement elem2 = groupG1.getUniformlyRandomNonNeutral();
        GroupElement elem3 = groupG1.getUniformlyRandomNonNeutral();

        elem1.pow(10).op(elem2.pow(10)).op(elem3.pow(10)).computeSync();

        GroupElement elemG1 = groupG1.getUniformlyRandomNonNeutral();
        GroupElement elemG2 = groupG2.getUniformlyRandomNonNeutral();
        bilGroup.getBilinearMap().apply(elemG1, elemG2, BigInteger.TEN).computeSync();

        elem1.op(elem2).inv().computeSync();

        elem1.getRepresentation();
        bilGroup.setBucket(bucketName2);
        elem1 = groupG1.getUniformlyRandomNonNeutral();
        elem2 = groupG1.getUniformlyRandomNonNeutral();
        elem3 = groupG1.getUniformlyRandomNonNeutral();

        elem1.pow(10).op(elem2.pow(10)).op(elem3.pow(10)).computeSync();

        elemG1 = groupG1.getUniformlyRandomNonNeutral();
        elemG2 = groupG2.getUniformlyRandomNonNeutral();
        bilGroup.getBilinearMap().apply(elemG1, elemG2, BigInteger.TEN).computeSync();

        elem1.op(elem2).inv().computeSync();

        elem1.getRepresentation();

        assertEquals(2, groupG1.getNumRetrievedRepresentationsAllBuckets());
        assertEquals(0, groupG1.getNumExpsAllBuckets());
        assertEquals(50, groupG1.getNumOpsTotalAllBuckets());
        assertEquals(8, groupG1.getNumSquaringsTotalAllBuckets());
        assertEquals(2, groupG1.getNumInversionsTotalAllBuckets());
        assertEquals(2, groupG1.getNumOpsNoExpMultiExpAllBuckets());
        assertEquals(0, groupG1.getNumSquaringsNoExpMultiExpAllBuckets());
        assertEquals(2, groupG1.getNumInversionsNoExpMultiExpAllBuckets());
        assertArrayEquals(new Integer[] {3, 3}, groupG1.getMultiExpTermNumbersAllBuckets().toArray(new Integer[] {}));

        assertEquals(0, groupG2.getNumRetrievedRepresentationsAllBuckets());
        assertEquals(0, groupG2.getNumExpsAllBuckets());
        assertEquals(0, groupG2.getNumOpsTotalAllBuckets());
        assertEquals(0, groupG2.getNumSquaringsTotalAllBuckets());
        assertEquals(0, groupG2.getNumInversionsTotalAllBuckets());
        assertEquals(0, groupG2.getNumOpsNoExpMultiExpAllBuckets());
        assertEquals(0, groupG2.getNumSquaringsNoExpMultiExpAllBuckets());
        assertEquals(0, groupG2.getNumInversionsNoExpMultiExpAllBuckets());
        assertArrayEquals(new Integer[] {}, groupG2.getMultiExpTermNumbersAllBuckets().toArray(new Integer[] {}));

        assertEquals(0, groupGT.getNumRetrievedRepresentationsAllBuckets());
        assertEquals(2, groupGT.getNumExpsAllBuckets());
        assertEquals(16, groupGT.getNumOpsTotalAllBuckets());
        assertEquals(4, groupGT.getNumSquaringsTotalAllBuckets());
        assertEquals(0, groupGT.getNumInversionsTotalAllBuckets());
        assertEquals(0, groupGT.getNumOpsNoExpMultiExpAllBuckets());
        assertEquals(0, groupGT.getNumSquaringsNoExpMultiExpAllBuckets());
        assertEquals(0, groupGT.getNumInversionsNoExpMultiExpAllBuckets());
        assertArrayEquals(new Integer[] {}, groupGT.getMultiExpTermNumbersAllBuckets().toArray(new Integer[] {}));

        assertEquals(2, bilGroup.getNumPairingsAllBuckets());
    }

    @Test
    public void testStaticCounting() {
        // test that both groups contain same numbers
        DebugGroup debug1 = new DebugGroup("DG1", 1_000_000);
        DebugGroup debug2 = new DebugGroup("DG2", 1_000_000);
        String bucketName = "#testStaticCounting";
        debug1.setBucket(bucketName);
        debug2.setBucket(bucketName);

        GroupElement elem1 = debug1.getUniformlyRandomNonNeutral();
        GroupElement elem2 = debug2.getUniformlyRandomNonNeutral();

        elem1.op(elem1).compute();
        elem2.op(elem2).compute();

        assertEquals(2, debug1.getNumSquaringsTotal(bucketName));
        assertEquals(2, debug2.getNumSquaringsTotal(bucketName));
    }

    @Test
    public void testSeparateBuckets() {
        String bucketName1 = getClass().getName() + "#testSeparateBuckets(1)";
        String bucketName2 = getClass().getName() + "#testSeparateBuckets(2)";
        DebugGroup debug1 = new DebugGroup("DG1", 1_000_000);
        GroupElement elem1 = debug1.getUniformlyRandomNonNeutral();

        debug1.setBucket(bucketName1);
        elem1.op(elem1).compute();

        debug1.setBucket(bucketName2);
        elem1.op(elem1).compute();

        assertEquals(1, debug1.getNumSquaringsTotal(bucketName1));
        assertEquals(1, debug1.getNumSquaringsTotal(bucketName2));
    }

    @Test
    public void testStaticPairingCounting() {
        String bucketName = getClass().getName() + "#testStaticPairingCounting";
        DebugBilinearGroup bilGroup1 = new DebugBilinearGroup(
                RandomGenerator.getRandomPrime(128), BilinearGroup.Type.TYPE_2
        );
        DebugGroup groupG1 = (DebugGroup) bilGroup.getG1();
        DebugGroup groupG2 = (DebugGroup) bilGroup.getG2();
        GroupElement elemG1 = groupG1.getUniformlyRandomNonNeutral();
        GroupElement elemG2 = groupG2.getUniformlyRandomNonNeutral();

        DebugGroup groupG11 = (DebugGroup) bilGroup1.getG1();
        DebugGroup groupG21 = (DebugGroup) bilGroup1.getG2();
        GroupElement elemG11 = groupG11.getUniformlyRandomNonNeutral();
        GroupElement elemG21 = groupG21.getUniformlyRandomNonNeutral();

        bilGroup.setBucket(bucketName);
        bilGroup1.setBucket(bucketName);

        bilGroup.getBilinearMap().apply(elemG1, elemG2).computeSync();
        bilGroup1.getBilinearMap().apply(elemG11, elemG21).computeSync();

        assertEquals(2, bilGroup.getNumPairings(bucketName));
        assertEquals(2, bilGroup1.getNumPairings(bucketName));
    }

    @Test
    public void testSeparateBucketsPairingCounting() {
        String bucketName1 = getClass().getName() + "#testSeparateBucketsPairingCounting(1)";
        String bucketName2 = getClass().getName() + "#testSeparateBucketsPairingCounting(2)";
        DebugGroup groupG1 = (DebugGroup) bilGroup.getG1();
        DebugGroup groupG2 = (DebugGroup) bilGroup.getG2();
        GroupElement elemG1 = groupG1.getUniformlyRandomNonNeutral();
        GroupElement elemG2 = groupG2.getUniformlyRandomNonNeutral();

        bilGroup.setBucket(bucketName1);
        bilGroup.getBilinearMap().apply(elemG1, elemG2).computeSync();

        bilGroup.setBucket(bucketName2);
        bilGroup.getBilinearMap().apply(elemG1, elemG2).computeSync();

        assertEquals(1, bilGroup.getNumPairings(bucketName1));
        assertEquals(1, bilGroup.getNumPairings(bucketName2));
    }
}
