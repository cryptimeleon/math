package org.cryptimeleon.math.structures.groups.lazy;

/**
 * Represents the result of inverting a group element.
 */
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
