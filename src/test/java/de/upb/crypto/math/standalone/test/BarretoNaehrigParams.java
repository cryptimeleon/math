package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.pairings.bn.*;

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
        // barretonaehrig group1
        toReturn.add(new StandaloneTestParams(BarretoNaehrigGroup1.class, bnGroup.getG1()));
        System.out.println(bnGroup.getG1().getRepresentation());
        // barreto naehrig group 2
        toReturn.add(new StandaloneTestParams(BarretoNaehrigGroup2.class, bnGroup.getG2()));
        // barreto naehrig field
        //toReturn.add(new StandaloneTestParams(BarretoNaehrigField.class, bnGroup.getG1().getFieldOfDefinition()));
        // barreto naehrig base field

        //	toReturn.add(new StandaloneTestParams(BarretoNaehrigBaseField.class, bnGroup.getG1().getA6().getStructure()));

        // barrot naehrig point encoding
        toReturn.add(new StandaloneTestParams(BarretoNaehrigPointEncoding.class,
                bnGroup.getHashIntoG1()));
        // barreto naehrig tate pairing
        toReturn.add(new StandaloneTestParams(BarretoNaehrigTatePairing.class,
                new BarretoNaehrigTatePairing(bnGroup.getG1(), bnGroup.getG2(), bnGroup.getGT())));
        // barreto naehrig parameters
        toReturn.add(new StandaloneTestParams(BarretoNaehrigBilinearGroup.class, bnGroup));
        // barreto naehrig target group
        toReturn.add(new StandaloneTestParams(BarretoNaehrigTargetGroup.class, bnGroup.getGT()));
        return toReturn;
    }
}
