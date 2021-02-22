package org.cryptimeleon.math.structures.groups.elliptic;

import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.rings.Field;

/**
 * Base class for elliptic curve group implementations.
 */
public interface EllipticCurve extends GroupImpl {

    /**
     * Returns the base field over which the elliptic curve is defined.
     */
    Field getFieldOfDefinition();

    @Override
    default boolean isCommutative() {
        return true;
    }
}
