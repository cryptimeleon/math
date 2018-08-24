package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.interfaces.mappings.IdentityIsomorphism;

public class IdentityIsomorphismParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(IdentityIsomorphism.class, new IdentityIsomorphism());
    }
}
