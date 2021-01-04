package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.pairings.type1.supersingular.SupersingularTateGroupImpl;

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

        return toReturn;
    }
}
