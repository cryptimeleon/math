package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.rings.zn.HashIntoZnAdditiveGroup;

import java.math.BigInteger;

public class HashIntoZnAdditiveGroupParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(HashIntoZnAdditiveGroup.class, new HashIntoZnAdditiveGroup(BigInteger.valueOf(1024)));
    }
}
