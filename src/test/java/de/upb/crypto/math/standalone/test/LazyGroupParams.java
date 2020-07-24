package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.interfaces.structures.group.impl.RingAdditiveGroupImpl;
import de.upb.crypto.math.structures.groups.lazy.LazyGroup;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class LazyGroupParams {
    public static List<StandaloneTestParams> get() {
        Zp zp = new Zp(BigInteger.valueOf(1103));
        LazyGroup group = new LazyGroup(new RingAdditiveGroupImpl(zp));
        return Arrays.asList(
                new StandaloneTestParams(group)
        );
    }
}
