package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.structures.zn.RingMultiplication;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;

public class RingMultiplicationParams {

    public static StandaloneTestParams get() {
        Zp zp = new Zp(BigInteger.valueOf(17));
        return new StandaloneTestParams(RingMultiplication.class, new RingMultiplication(zp));
    }
}
