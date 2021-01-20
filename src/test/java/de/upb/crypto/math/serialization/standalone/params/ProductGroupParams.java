package de.upb.crypto.math.serialization.standalone.params;

import de.upb.crypto.math.structures.groups.Group;
import de.upb.crypto.math.serialization.standalone.StandaloneTestParams;
import de.upb.crypto.math.structures.groups.cartesian.ProductGroup;
import de.upb.crypto.math.structures.rings.zn.Zn;

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
