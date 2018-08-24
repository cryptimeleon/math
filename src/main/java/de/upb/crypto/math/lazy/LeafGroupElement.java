package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;

import javax.annotation.Nonnull;

/**
 * A lazy group element whose value is set from the beginning.
 */
public class LeafGroupElement extends LeafElement {
    /**
     * Instantiates a leaf group element
     *
     * @param group
     * @param value from the baseGroup
     */
    public LeafGroupElement(LazyGroup group, @Nonnull GroupElement value) {
        super(group);
        this.value = value;
        if (value == null)
            throw new IllegalArgumentException("Unexpected null");
    }

    @Override
    protected void putProduct(PowProductExpression prod, PairingProductExpression pairingProd) {
        prod.op(value);
    }

    @Override
    protected void putProductBasedOnLeafs(PowProductExpression prod, PairingProductExpression pairingProd) {
        prod.op(value);
    }
}
