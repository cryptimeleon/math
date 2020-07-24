package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;

public class InvLazyGroupElement extends LazyGroupElement {
    LazyGroupElement base;

    public InvLazyGroupElement(LazyGroup group, LazyGroupElement base) {
        super(group);
        this.base = base;
    }

    @Override
    protected GroupElementImpl computeConcreteValue() {
        return base.getConcreteGroupElement().inv();
    }
}
