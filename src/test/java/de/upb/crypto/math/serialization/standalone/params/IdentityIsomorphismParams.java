package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.structures.groups.mappings.IdentityIsomorphism;
import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;

public class IdentityIsomorphismParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(IdentityIsomorphism.class, new IdentityIsomorphism());
    }
}
