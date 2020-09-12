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
    protected void computeConcreteValue() {
        setConcreteValue(group.compute(base.getConcreteValue(), exponent, base.getPrecomputedSmallExponents()));
    }

    @Override
    protected GroupElementImpl accumulateMultiexp(Multiexponentiation multiexp) {
        if (isDefinitelySupposedToGetConcreteValue())
            return getConcreteValue();

        multiexp.put(base.getConcreteValue(), exponent, base.getPrecomputedSmallExponents());
        return null;
    }
}
