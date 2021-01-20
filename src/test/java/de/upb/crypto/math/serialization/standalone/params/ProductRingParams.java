package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.structures.rings.Ring;
import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.rings.cartesian.ProductRing;
import de.upb.crypto.math.structures.rings.zn.Zn;
import de.upb.crypto.math.structures.rings.zn.Zp;

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
