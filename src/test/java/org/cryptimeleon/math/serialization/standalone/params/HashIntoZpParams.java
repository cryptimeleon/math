package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZp;
import org.cryptimeleon.math.structures.rings.zn.Zp;

import java.math.BigInteger;

public class HashIntoZpParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new HashIntoZp(new Zp(BigInteger.valueOf(1103))));
    }
}
