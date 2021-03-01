package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.hash.impl.VariableOutputLengthHashFunction;
import org.cryptimeleon.math.serialization.standalone.StandaloneReprSubTest;

public class HashStandaloneReprTest extends StandaloneReprSubTest {
    public void testVariableLengthHash() {
        test(new VariableOutputLengthHashFunction(10));
    }
}
