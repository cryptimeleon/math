package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.pairings.type3.bn.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BarretoNaehrigParams {
    public static Collection<StandaloneTestParams> get() {
        // this is a barreto naehrig factory
        BarretoNaehrigBilinearGroupImpl bnGroup = new BarretoNaehrigBilinearGroupImpl(100);

        List<StandaloneTestParams> toReturn = new ArrayList<>();
        toReturn.add(new StandaloneTestParams(bnGroup.getG1()));
        toReturn.add(new StandaloneTestParams(bnGroup.getG2()));
        toReturn.add(new StandaloneTestParams(bnGroup.getHashIntoG1()));
        toReturn.add(new StandaloneTestParams(bnGroup));
        toReturn.add(new StandaloneTestParams(bnGroup.getGT()));
        return toReturn;
    }
}
