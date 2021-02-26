package org.cryptimeleon.math.structures.groups.lazy;

import org.cryptimeleon.math.structures.groups.GroupElementImpl;

/**
 * Represents the result of generating a non-neutral group element uniformly at random.
 */
public class RandomNonNeutralGroupElement extends LazyGroupElement {
    private GroupElementImpl value = null;

    public RandomNonNeutralGroupElement(LazyGroup group) {
        super(group);
    }

    @Override
    protected synchronized void computeConcreteValue() {
        if (value == null)
            value = group.impl.getUniformlyRandomElement();

        setConcreteValue(value);
    }
}
