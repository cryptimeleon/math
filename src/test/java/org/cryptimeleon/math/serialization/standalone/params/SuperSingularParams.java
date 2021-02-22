package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular.SupersingularBilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular.SupersingularTateGroupImpl;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SuperSingularParams {
    public static Collection<StandaloneTestParams> get() {
        SupersingularTateGroupImpl supsingGrp = new SupersingularTateGroupImpl(80);
        List<StandaloneTestParams> toReturn = new ArrayList<>();

        toReturn.add(new StandaloneTestParams(supsingGrp));
        toReturn.add(new StandaloneTestParams(supsingGrp.getG1()));
        toReturn.add(new StandaloneTestParams(supsingGrp.getGT()));
        toReturn.add(new StandaloneTestParams(supsingGrp.getHashIntoG1()));
        toReturn.add(new StandaloneTestParams(new SupersingularBilinearGroup(80)));

        return toReturn;
    }
}
