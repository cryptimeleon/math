package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.structures.zn.HashIntoZn;

import java.math.BigInteger;

public class HashIntoZnParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(HashIntoZn.class, new HashIntoZn(BigInteger.valueOf(256)));
    }
}
