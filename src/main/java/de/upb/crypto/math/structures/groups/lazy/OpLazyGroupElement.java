package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;

public class OpLazyGroupElement extends LazyGroupElement {
    LazyGroupElement lhs, rhs;

    public OpLazyGroupElement(LazyGroup group, LazyGroupElement lhs, LazyGroupElement rhs) {
        super(group);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    protected GroupElementImpl computeConcreteValue() {
        Multiexponentiation multiexp = new Multiexponentiation();
        this.accumulateMultiexp(multiexp);

        return group.compute(multiexp);
    }

    @Override
    protected void accumulateMultiexp(Multiexponentiation multiexp) {
        lhs.accumulateMultiexp(multiexp);
        rhs.accumulateMultiexp(multiexp);
    }
}
