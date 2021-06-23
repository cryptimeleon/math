package org.cryptimeleon.math.structures.groups.lazy;

import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;

/**
 * Represents the neutral group element in the lazy evaluation framework.
 */
class NeutralLazyGroupElement extends LazyGroupElement {
    public NeutralLazyGroupElement(LazyGroup group) {
        super(group, group.impl.getNeutralElement());
    }

    @Override
    protected void computeConcreteValue() {
        //Already set in constructor
    }

    @Override
    protected GroupElementImpl accumulateMultiexp(Multiexponentiation multiexp) {
        //Nothing to do here.
        return null;
    }
}
