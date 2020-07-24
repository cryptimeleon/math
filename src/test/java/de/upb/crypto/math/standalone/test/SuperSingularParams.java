package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.pairings.supersingular.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SuperSingularParams {
    public static Collection<StandaloneTestParams> get() {
        SupersingularProvider fac = new SupersingularProvider();
        SupersingularTateGroup supsingGrp = fac.provideBilinearGroup(80, new BilinearGroupRequirement(BilinearGroup.Type.TYPE_1, true, true, false));
        List<StandaloneTestParams> toReturn = new ArrayList<>();

        toReturn.add(new StandaloneTestParams(supsingGrp));
        toReturn.add(new StandaloneTestParams(supsingGrp.getG1()));
        toReturn.add(new StandaloneTestParams(supsingGrp.getGT()));
        toReturn.add(new StandaloneTestParams(supsingGrp.getHashIntoG1()));

        return toReturn;
    }
}
