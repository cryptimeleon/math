package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.sn.Sn;

public class SnParams {


    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new Sn(50));
    }
}
