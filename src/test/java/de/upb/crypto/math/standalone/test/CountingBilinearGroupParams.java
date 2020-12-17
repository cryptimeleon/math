package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.pairings.counting.CountingBilinearGroup;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class CountingBilinearGroupParams {

    public static List<StandaloneTestParams> get() {
        CountingBilinearGroup group = new CountingBilinearGroup(BilinearGroup.Type.TYPE_1, BigInteger.valueOf(1000), true);
        return Arrays.asList(new StandaloneTestParams(group),
                new StandaloneTestParams(group.getG1()),
                new StandaloneTestParams(group.getHashIntoG1()),
                new StandaloneTestParams(group.getHomomorphismG2toG1())
        );
    }
}
