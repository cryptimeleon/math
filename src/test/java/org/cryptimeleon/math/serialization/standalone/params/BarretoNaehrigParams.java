package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.structures.groups.elliptic.type3.bn.BarretoNaehrigBilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.type3.bn.BarretoNaehrigBilinearGroupImpl;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BarretoNaehrigParams {
    public static Collection<StandaloneTestParams> get() {
        BarretoNaehrigBilinearGroupImpl bnGroupImpl = new BarretoNaehrigBilinearGroupImpl("SFC-256");

        List<StandaloneTestParams> toReturn = new ArrayList<>();
        toReturn.add(new StandaloneTestParams(bnGroupImpl.getG1()));
        toReturn.add(new StandaloneTestParams(bnGroupImpl.getG2()));
        toReturn.add(new StandaloneTestParams(bnGroupImpl.getHashIntoG1()));
        toReturn.add(new StandaloneTestParams(bnGroupImpl));
        toReturn.add(new StandaloneTestParams(bnGroupImpl.getGT()));
        toReturn.add(new StandaloneTestParams(new BarretoNaehrigBilinearGroup("SFC-256")));
        return toReturn;
    }
}
