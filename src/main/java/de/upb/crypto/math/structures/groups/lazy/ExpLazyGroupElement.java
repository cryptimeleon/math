package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;

import java.math.BigInteger;

public class ExpLazyGroupElement extends LazyGroupElement {
    LazyGroupElement base;
    BigInteger exponent;

    public ExpLazyGroupElement(LazyGroup group, LazyGroupElement base, BigInteger exponent) {
        super(group);
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    protected GroupElementImpl computeConcreteValue() {
        return group.compute(base.getConcreteGroupElement(), exponent, base.precomputedSmallExponents);
    }

    @Override
    protected void accumulateMultiexp(Multiexponentiation multiexp) {
        multiexp.put(base.getConcreteGroupElement(), exponent, base.precomputedSmallExponents);
    }
}
