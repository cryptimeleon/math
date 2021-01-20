package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.rings.zn.HashIntoZnAdditiveGroup;

import java.math.BigInteger;

public class HashIntoZnAdditiveGroupParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(HashIntoZnAdditiveGroup.class, new HashIntoZnAdditiveGroup(BigInteger.valueOf(1024)));
    }
}
