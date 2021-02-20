package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.structures.groups.mappings.IdentityIsomorphism;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;

public class IdentityIsomorphismParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(IdentityIsomorphism.class, new IdentityIsomorphism());
    }
}
