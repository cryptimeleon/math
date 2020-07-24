package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.pairings.bn.*;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BarretoNaehrigParams {
    public static Collection<StandaloneTestParams> get() {
        // this is a barreto naehrig factory
        BarretoNaehrigProvider fac = new BarretoNaehrigProvider();
        BilinearGroupRequirement req = new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3, true, true, false);
        BarretoNaehrigBilinearGroup bnGroup = fac.provideBilinearGroup(256, req);

        List<StandaloneTestParams> toReturn = new ArrayList<>();
        toReturn.add(new StandaloneTestParams(bnGroup.getG1()));
        toReturn.add(new StandaloneTestParams(bnGroup.getG2()));
        toReturn.add(new StandaloneTestParams(bnGroup.getHashIntoG1()));
        toReturn.add(new StandaloneTestParams(bnGroup));
        toReturn.add(new StandaloneTestParams(bnGroup.getGT()));
        return toReturn;
    }
}
