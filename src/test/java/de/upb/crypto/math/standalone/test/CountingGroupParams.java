package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.pairings.debug.CountingGroup;

public class CountingGroupParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new CountingGroup("STest", 100000));
    }
}
