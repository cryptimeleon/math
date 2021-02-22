package org.cryptimeleon.math.serialization.standalone.params;


import org.cryptimeleon.math.structures.groups.elliptic.nopairing.Secp256k1;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;

import java.util.Arrays;
import java.util.List;

public class Secp256k1Params {
    public static List<StandaloneTestParams> get() {
        return Arrays.asList(new StandaloneTestParams(new Secp256k1()), new StandaloneTestParams(new Secp256k1.HashIntoSecp256k1()));
    }
}
