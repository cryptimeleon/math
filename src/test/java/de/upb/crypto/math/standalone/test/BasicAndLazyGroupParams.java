package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupImpl;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.structures.group.impl.RingAdditiveGroupImpl;
import de.upb.crypto.math.pairings.debug.DebugBilinearGroupProvider;
import de.upb.crypto.math.structures.groups.basic.BasicBilinearGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyBilinearGroup;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class BasicAndLazyGroupParams {
    public static List<StandaloneTestParams> get() {
        BilinearGroupImpl debugGroupImpl = new DebugBilinearGroupProvider().provideBilinearGroupImpl(128, new BilinearGroupRequirement(BilinearGroup.Type.TYPE_1, true, false, false));
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
