package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.rings.zn.Zp;

import java.math.BigInteger;

public class ZpParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(Zp.class, new Zp(BigInteger.valueOf(17)));
    }
}
