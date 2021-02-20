package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.structures.groups.counting.CountingBilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroupImpl;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.groups.basic.BasicBilinearGroup;
import org.cryptimeleon.math.structures.groups.lazy.LazyBilinearGroup;

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
