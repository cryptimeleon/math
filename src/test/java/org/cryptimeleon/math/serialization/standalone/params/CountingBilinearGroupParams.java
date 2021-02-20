package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.structures.groups.counting.CountingBilinearGroup;
import org.cryptimeleon.math.structures.groups.counting.CountingBilinearGroupImpl;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroupImpl;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;

import java.util.Arrays;
import java.util.List;

public class CountingBilinearGroupParams {

    public static List<StandaloneTestParams> get() {
        BilinearGroup group = new CountingBilinearGroup(128, BilinearGroup.Type.TYPE_1);
        BilinearGroupImpl groupImpl = new CountingBilinearGroupImpl(128, BilinearGroup.Type.TYPE_1);
        return Arrays.asList(new StandaloneTestParams(group),
                new StandaloneTestParams(group.getG1()),
                new StandaloneTestParams(group.getHashIntoG1()),
                new StandaloneTestParams(group.getHomomorphismG2toG1()),
                new StandaloneTestParams(groupImpl),
                new StandaloneTestParams(groupImpl.getG1()),
                new StandaloneTestParams(groupImpl.getHashIntoG1()),
                new StandaloneTestParams(groupImpl.getHomomorphismG2toG1())
        );
    }
}
