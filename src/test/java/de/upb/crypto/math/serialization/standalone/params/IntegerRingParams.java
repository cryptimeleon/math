package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.rings.integers.IntegerRing;

public class IntegerRingParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(IntegerRing.class, new IntegerRing());
    }
}
