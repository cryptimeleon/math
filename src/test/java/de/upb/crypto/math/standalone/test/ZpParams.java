package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;

public class ZpParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(Zp.class, new Zp(BigInteger.valueOf(17)));
    }
}
