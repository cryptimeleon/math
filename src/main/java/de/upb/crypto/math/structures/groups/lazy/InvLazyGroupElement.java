package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

public class InvLazyGroupElement extends LazyGroupElement {
    protected LazyGroupElement base;

    public InvLazyGroupElement(LazyGroup group, LazyGroupElement base) {
        super(group);
        this.base = base;
    }

    @Override
    protected void computeConcreteValue() {
        setConcreteValue(base.getConcreteValue().inv());
    }
}
