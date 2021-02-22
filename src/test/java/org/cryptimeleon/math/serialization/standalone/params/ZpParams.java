package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.rings.zn.Zp;

import java.math.BigInteger;

public class ZpParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(Zp.class, new Zp(BigInteger.valueOf(17)));
    }
}
