package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.hash.impl.VariableOutputLengthHashFunction;
import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;

public class VariableOutputLengthHashFunctionParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new VariableOutputLengthHashFunction(10));
    }
}
