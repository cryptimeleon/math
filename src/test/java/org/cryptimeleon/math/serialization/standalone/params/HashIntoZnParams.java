package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZn;

import java.math.BigInteger;

public class HashIntoZnParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(HashIntoZn.class, new HashIntoZn(BigInteger.valueOf(256)));
    }
}
