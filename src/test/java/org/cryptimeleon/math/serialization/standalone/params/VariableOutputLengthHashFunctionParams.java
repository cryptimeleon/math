package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.hash.impl.VariableOutputLengthHashFunction;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;

public class VariableOutputLengthHashFunctionParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new VariableOutputLengthHashFunction(10));
    }
}
