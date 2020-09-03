package de.upb.crypto.math.test;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.debug.CountingGroup;

import java.math.BigInteger;

public class ManualTest {
    public static void main(String[] args) {
        CountingGroup countingGroup = new CountingGroup("test", BigInteger.valueOf(1000000));
        GroupElement elem = countingGroup.getUniformlyRandomNonNeutral();
        GroupElement elem2 = countingGroup.getUniformlyRandomNonNeutral();
        GroupElement elem3 = countingGroup.getUniformlyRandomNonNeutral();
        GroupElement elem4 = countingGroup.getUniformlyRandomNonNeutral();

        elem.pow(10).op(elem2.pow(10)).op(elem3.pow(10)).op(elem4.pow(10)).compute();

        elem.getRepresentation();

        elem.pow(10).computeSync();

        elem.op(elem).op(elem2).inv().compute();

        System.out.println(countingGroup.formatCounters());
    }
}
