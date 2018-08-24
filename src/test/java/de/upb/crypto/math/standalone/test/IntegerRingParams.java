package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.structures.integers.IntegerRing;

public class IntegerRingParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(IntegerRing.class, new IntegerRing());
    }
}
