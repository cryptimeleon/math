package org.cryptimeleon.math.structures.groups.lazy;

import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;

import java.math.BigInteger;

/**
 * Represents an exponentiation with a base and exponent.
 */
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
