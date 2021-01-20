package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.pairings.counting.CountingBilinearGroupImpl;
import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.pairings.generic.BilinearGroupImpl;
import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.groups.basic.BasicBilinearGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearGroup;

import java.util.Arrays;
import java.util.List;

public class BasicAndLazyGroupParams {
    public static List<StandaloneTestParams> get() {
        BilinearGroupImpl debugGroupImpl = new CountingBilinearGroupImpl(128, BilinearGroup.Type.TYPE_1);
        LazyBilinearGroup lazy = new LazyBilinearGroup(debugGroupImpl);
        BasicBilinearGroup basic = new BasicBilinearGroup(debugGroupImpl);

        return Arrays.asList(
                new StandaloneTestParams(lazy),
                new StandaloneTestParams(lazy.getG1()),
                new StandaloneTestParams(lazy.getHomomorphismG2toG1()),
                new StandaloneTestParams(lazy.getHashIntoG1()),

                new StandaloneTestParams(basic),
                new StandaloneTestParams(basic.getG1()),
                new StandaloneTestParams(basic.getHomomorphismG2toG1()),
                new StandaloneTestParams(basic.getHashIntoG1())
        );
    }
}
