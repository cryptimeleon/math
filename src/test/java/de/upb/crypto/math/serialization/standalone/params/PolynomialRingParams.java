package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.polynomial.PolynomialRing;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;

public class PolynomialRingParams {
    public static StandaloneTestParams get() {
        return new StandaloneTestParams(PolynomialRing.class, new PolynomialRing(new Zp(BigInteger.valueOf(17))));
    }
}
