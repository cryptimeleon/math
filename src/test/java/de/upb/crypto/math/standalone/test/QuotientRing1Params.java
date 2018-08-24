package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.structures.test.QuotientRingZ13TestImpl;

public class QuotientRing1Params {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new QuotientRingZ13TestImpl());
    }
}
