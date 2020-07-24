package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;

public interface EllipticCurve extends GroupImpl {

    public Field getFieldOfDefinition();

    @Override
    default boolean isCommutative() {
        return true;
    }
}
