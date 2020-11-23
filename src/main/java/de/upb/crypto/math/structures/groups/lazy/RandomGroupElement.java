package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;

/**
 * Represents the result of generating a group element unformly at random.
 */
public class RandomGroupElement extends LazyGroupElement {
    private GroupElementImpl value = null;

    public RandomGroupElement(LazyGroup group) {
        super(group);
    }

    @Override
    protected synchronized void computeConcreteValue() {
        if (value == null)
            value = group.impl.getUniformlyRandomElement();

        setConcreteValue(value);
    }
}
