package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.rings.bool.BooleanStructure;

public class BooleanStructureParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new BooleanStructure());
    }
}
