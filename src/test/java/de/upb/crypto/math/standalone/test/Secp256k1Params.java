package de.upb.crypto.math.standalone.test;


import de.upb.crypto.math.elliptic.Secp256k1;

public class Secp256k1Params {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new Secp256k1());
    }
}
