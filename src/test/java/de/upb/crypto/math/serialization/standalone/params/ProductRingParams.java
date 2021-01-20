package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.cartesian.ProductRing;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;

public class ProductRingParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new ProductRing(
                new Ring[] {
                        new Zp(BigInteger.valueOf(101)),
                        new Zn(BigInteger.valueOf(33))
                }
        ));
    }
}
