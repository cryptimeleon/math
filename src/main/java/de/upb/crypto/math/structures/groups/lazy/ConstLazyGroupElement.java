package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

/**
 * Represents a constant value.
 */
public class ConstLazyGroupElement extends LazyGroupElement {
    public ConstLazyGroupElement(LazyGroup group, GroupElementImpl concreteValue) {
        super(group, concreteValue);
    }

    @Override
    protected void computeConcreteValue() {
        //nothing to do, value is already known from constructor call
    }
}
