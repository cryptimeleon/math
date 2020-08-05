package de.upb.crypto.math.test;

import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.debug.DebugGroupElementImpl;
import de.upb.crypto.math.pairings.debug.DebugGroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.converter.BinaryFormatConverter;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

public class ManualTest {
    public static void main(String[] args) {
        DebugGroupImpl debugGroup = new DebugGroupImpl("D1", BigInteger.valueOf(1000000));
        LazyGroup lazyGroup = new LazyGroup(debugGroup);
        GroupElement elem = lazyGroup.getUniformlyRandomNonNeutral();
        GroupElement elem2 = lazyGroup.getUniformlyRandomNonNeutral();
        Representation repr = lazyGroup.getRepresentation();

        System.out.println("Ops: " + debugGroup.getNumOps());
        System.out.println("Sqs: " + debugGroup.getNumSquarings());
        System.out.println("Invs: " + debugGroup.getNumInversions());
        System.out.println("Exps: " + debugGroup.getNumExps());
        System.out.println("MultiExps: " + debugGroup.getMultiExpData().toString());
    }
}
