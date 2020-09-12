package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;

public class NeutralLazyGroupElement extends LazyGroupElement {
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
