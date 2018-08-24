package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.hash.impl.VariableOutputLengthHashFunction;

public class VariableOutputLengthHashFunctionParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new VariableOutputLengthHashFunction(10));
    }
}
