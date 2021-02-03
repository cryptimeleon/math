package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.rings.zn.HashIntoZn;

import java.math.BigInteger;

public class HashIntoZnParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(HashIntoZn.class, new HashIntoZn(BigInteger.valueOf(256)));
    }
}
