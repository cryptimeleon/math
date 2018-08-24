package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.structures.zn.HashIntoZnAdditiveGroup;

import java.math.BigInteger;

public class HashIntoZnAdditiveGroupParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(HashIntoZnAdditiveGroup.class, new HashIntoZnAdditiveGroup(BigInteger.valueOf(1024)));
    }
}
