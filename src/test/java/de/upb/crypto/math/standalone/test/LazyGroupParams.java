package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.lazy.HashIntoLazyGroup;
import de.upb.crypto.math.lazy.LazyBilinearGroup;
import de.upb.crypto.math.lazy.LazyGroup;
import de.upb.crypto.math.lazy.LazyPairing;
import de.upb.crypto.math.pairings.debug.DebugBilinearGroupProvider;
import de.upb.crypto.math.structures.zn.HashIntoZn;
import de.upb.crypto.math.structures.zn.RingMultiplication;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class LazyGroupParams {
    public static List<StandaloneTestParams> get() {
        Zp zp = new Zp(BigInteger.valueOf(1103));
        DebugBilinearGroupProvider debugBilinearGroupProvider = new DebugBilinearGroupProvider().provideBilinearGroup(50);
        LazyBilinearGroup fac = new LazyBilinearGroup(debugBilinearGroupProvider);
        LazyGroup group = new LazyGroup(new RingUnitGroup(zp));
        return Arrays.asList(
                new StandaloneTestParams(fac),
                new StandaloneTestParams(group),
                new StandaloneTestParams(new LazyPairing(new RingMultiplication(zp))),
                new StandaloneTestParams(new HashIntoLazyGroup(group, new HashIntoZn(zp)))
        );
    }
}
