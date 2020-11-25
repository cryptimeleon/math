package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;

public interface EllipticCurve extends GroupImpl {

    /**
     * Returns the base field over which the elliptic curve is defined.
     */
    public Field getFieldOfDefinition();

    @Override
    default boolean isCommutative() {
        return true;
    }
}
