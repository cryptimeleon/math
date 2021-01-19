package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;

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
