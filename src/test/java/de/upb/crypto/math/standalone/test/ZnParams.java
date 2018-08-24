package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

public class ZnParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(Zn.class, new Zn(BigInteger.valueOf(17)));
    }
}
