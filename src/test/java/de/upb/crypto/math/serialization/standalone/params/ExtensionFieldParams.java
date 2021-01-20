package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.structures.rings.extfield.ExtensionField;
import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;

import java.math.BigInteger;

public class ExtensionFieldParams {


    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new ExtensionField(BigInteger.valueOf(17)));
    }
}
