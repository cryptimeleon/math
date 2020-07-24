package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.pairings.debug.DebugGroupImpl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class DebugBilinearGroup {

    public static List<StandaloneTestParams> get() {
        de.upb.crypto.math.pairings.debug.DebugBilinearGroup fac = new de.upb.crypto.math.pairings.debug.DebugBilinearGroup(BilinearGroup.Type.TYPE_1, BigInteger.valueOf(1000), true);
        return Arrays.asList(new StandaloneTestParams(fac),
                new StandaloneTestParams(fac.getG1()),
                new StandaloneTestParams(new DebugGroupImpl("test", BigInteger.valueOf(1000))),
                new StandaloneTestParams(fac.getHashIntoG1()),
                new StandaloneTestParams(fac.getHomomorphismG2toG1())
        );
    }
}
