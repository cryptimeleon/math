package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.groups.sn.Sn;

public class SnParams {


    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new Sn(50));
    }
}
