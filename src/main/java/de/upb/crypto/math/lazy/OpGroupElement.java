package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.expressions.PowProductExpression;

import javax.annotation.Nonnull;

public class OpGroupElement extends LazyGroupElement {
    protected LazyGroupElement g, h;

    public OpGroupElement(@Nonnull LazyGroup group, @Nonnull LazyGroupElement g, @Nonnull LazyGroupElement h) {
        super(group);
        this.g = g;
        this.h = h;
        g.registerReference();
        h.registerReference();
        group.unregisterUncomputedRoot(g);
        group.unregisterUncomputedRoot(h);
        group.registerUncomputedRoot(this);
    }

    @Override
    protected void putProduct(PowProductExpression prod, PairingProductExpression pairingProd) {
        if (value != null) {
            prod.op(value);
        } else {
            g.putProduct(prod, pairingProd);
            h.putProduct(prod, pairingProd);
        }
    }

    @Override
    protected void putProductBasedOnLeafs(PowProductExpression prod, PairingProductExpression pairingProd) {
        g.putProductBasedOnLeafs(prod, pairingProd);
        h.putProductBasedOnLeafs(prod, pairingProd);
    }
}
