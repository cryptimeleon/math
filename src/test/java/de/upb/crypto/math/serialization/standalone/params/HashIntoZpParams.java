package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.zn.HashIntoZp;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;

public class HashIntoZpParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new HashIntoZp(new Zp(BigInteger.valueOf(1103))));
    }
}
