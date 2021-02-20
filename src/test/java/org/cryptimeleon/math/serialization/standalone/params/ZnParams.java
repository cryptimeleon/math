package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

public class ZnParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(Zn.class, new Zn(BigInteger.valueOf(17)));
    }
}
