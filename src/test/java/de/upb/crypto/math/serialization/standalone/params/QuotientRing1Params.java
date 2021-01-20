package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.QuotientRingZ13TestImpl;

public class QuotientRing1Params {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new QuotientRingZ13TestImpl());
    }
}
