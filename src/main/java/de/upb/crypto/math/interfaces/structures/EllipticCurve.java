package de.upb.crypto.math.interfaces.structures;

public interface EllipticCurve extends Group {

    public Field getFieldOfDefinition();

    @Override
    default boolean isCommutative() {
        return true;
    }
}
