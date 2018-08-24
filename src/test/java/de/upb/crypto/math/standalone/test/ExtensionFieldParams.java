package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.pairings.generic.ExtensionField;

import java.math.BigInteger;

public class ExtensionFieldParams {


    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new ExtensionField(BigInteger.valueOf(17)));
    }
}
