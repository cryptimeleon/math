package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.bool.BooleanStructure;

public class BooleanStructureParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new BooleanStructure());
    }
}
