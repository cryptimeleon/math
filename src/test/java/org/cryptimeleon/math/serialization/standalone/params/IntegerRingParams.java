package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.rings.integers.IntegerRing;

public class IntegerRingParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(IntegerRing.class, new IntegerRing());
    }
}
