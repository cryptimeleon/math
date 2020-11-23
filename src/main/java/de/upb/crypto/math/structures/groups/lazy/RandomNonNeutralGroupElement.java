package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

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
