package de.upb.crypto.math.lazy;

/**
 * Smallest expressions that can be handled by PowProductExpression/PairingProductExpression
 */
public abstract class LeafElement extends LazyGroupElement {
    protected LeafElement(LazyGroup group) {
        super(group);
    }
}
