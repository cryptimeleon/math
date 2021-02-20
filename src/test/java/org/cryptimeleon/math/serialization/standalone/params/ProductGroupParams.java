package org.cryptimeleon.math.serialization.standalone.params;

import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.serialization.standalone.StandaloneTestParams;
import org.cryptimeleon.math.structures.groups.cartesian.ProductGroup;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

public class ProductGroupParams {

    public static StandaloneTestParams get() {
        return new StandaloneTestParams(new ProductGroup(
                new Group[] {
                        new Zn(BigInteger.valueOf(100)).asAdditiveGroup(),
                        new Zn(BigInteger.valueOf(33)).asUnitGroup()
                }
        ));
    }
}
