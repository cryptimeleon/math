package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.rings.cartesian.ProductRing;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zp;

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
