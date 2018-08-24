package de.upb.crypto.math.standalone.test;

import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;

public class RingUnitGroupParams {
    public static StandaloneTestParams get() {
        Zp zp = new Zp(BigInteger.valueOf(17));
        return new StandaloneTestParams(RingUnitGroup.class, zp.asUnitGroup());
    }
}
