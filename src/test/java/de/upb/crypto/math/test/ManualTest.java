package de.upb.crypto.math.test;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.debug.DebugGroupElementImpl;
import de.upb.crypto.math.pairings.debug.DebugGroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.converter.BinaryFormatConverter;
import de.upb.crypto.math.structures.groups.count.CountingGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

public class ManualTest {
    public static void main(String[] args) {
        CountingGroup countingGroup = new CountingGroup("test", BigInteger.valueOf(1000000));
        GroupElement elem = countingGroup.getUniformlyRandomNonNeutral();
        GroupElement elem2 = countingGroup.getUniformlyRandomNonNeutral();

        for (int i = 0; i < 10; ++i) {
            elem.op(elem2).compute();
        }

        /*System.out.println("Ops: " + debugGroup.getNumOps());
        System.out.println("Sqs: " + debugGroup.getNumSquarings());
        System.out.println("Invs: " + debugGroup.getNumInversions());
        System.out.println("Exps: " + debugGroup.getNumExps());
        System.out.println("MultiExps: " + debugGroup.getMultiExpData().toString());
        System.out.println("Retrieved Representations: " + debugGroup.getNumRetrievedRepresentations());*/
    }
}
