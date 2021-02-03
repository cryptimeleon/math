package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.rings.zn.Zn;

import java.math.BigInteger;

public class ZnParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(Zn.class, new Zn(BigInteger.valueOf(17)));
    }
}
