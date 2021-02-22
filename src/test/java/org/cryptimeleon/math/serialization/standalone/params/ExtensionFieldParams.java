package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;

import java.math.BigInteger;

public class ExtensionFieldParams {


    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new ExtensionField(BigInteger.valueOf(17)));
    }
}
