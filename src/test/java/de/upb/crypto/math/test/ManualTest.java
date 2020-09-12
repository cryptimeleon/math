package de.upb.crypto.math.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.debug.count.CountingBilinearGroup;
import de.upb.crypto.math.pairings.debug.count.CountingBilinearGroupProvider;
import de.upb.crypto.math.pairings.debug.count.CountingGroup;

import java.math.BigInteger;

public class ManualTest {
    public static void main(String[] args) {
        CountingBilinearGroup bilGroup = (CountingBilinearGroup) new CountingBilinearGroupProvider()
                .provideBilinearGroup(
                    128,
                    new BilinearGroupRequirement(
                            BilinearGroup.Type.TYPE_1, true, true, true
                    )
        );

        CountingGroup countingGroup = (CountingGroup) bilGroup.getG1();
        GroupElement elem = countingGroup.getUniformlyRandomNonNeutral();
        GroupElement elem2 = countingGroup.getUniformlyRandomNonNeutral();
        GroupElement elem3 = countingGroup.getUniformlyRandomNonNeutral();
        GroupElement elem4 = countingGroup.getUniformlyRandomNonNeutral();

        //elem.pow(10).op(elem2.pow(10)).op(elem3.pow(10)).op(elem4.pow(10)).compute();

        elem.getRepresentation();

        //elem.pow(10).computeSync();

        //elem.op(elem).op(elem2).inv().compute();

        CountingGroup G2 = (CountingGroup) bilGroup.getG2();
        GroupElement elemG2 = G2.getUniformlyRandomNonNeutral();
        bilGroup.getBilinearMap().apply(elem, elemG2).computeSync();
        //bilGroup.getBilinearMap().apply(elem, elemG2, BigInteger.TEN).computeSync();

        System.out.println(bilGroup.formatCounterData());

        bilGroup.resetNumPairings();

        System.out.println(bilGroup.getNumPairings());
    }
}
