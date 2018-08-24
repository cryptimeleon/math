package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.pairings.debug.DebugBilinearGroupProvider;

import java.util.Arrays;
import java.util.List;

public class DebugBilinearGroup {

    public static List<StandaloneTestParams> get() {
        DebugBilinearGroupProvider fac = new DebugBilinearGroupProvider().provideBilinearGroup(80);
        return Arrays.asList(new StandaloneTestParams(fac),
                new StandaloneTestParams(fac.getBilinearMap()),
                new StandaloneTestParams(fac.getG1()),
                new StandaloneTestParams(fac.getHashIntoG1()),
                new StandaloneTestParams(fac.getHomomorphismG2toG1())
        );
    }
}
