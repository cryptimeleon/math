package org.cryptimeleon.math.structures.groups.lazy;

import org.cryptimeleon.math.structures.groups.GroupElementImpl;

/**
 * Represents a constant value.
 */
class ConstLazyGroupElement extends LazyGroupElement {
    public ConstLazyGroupElement(LazyGroup group, GroupElementImpl concreteValue) {
        super(group, concreteValue);
    }

    @Override
    protected void computeConcreteValue() {
        //nothing to do, value is already known from constructor call
    }
}
