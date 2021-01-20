package de.upb.crypto.math.serialization.standalone.params;


import de.upb.crypto.math.elliptic.Secp256k1;
import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;

public class Secp256k1Params {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new Secp256k1());
    }
}
