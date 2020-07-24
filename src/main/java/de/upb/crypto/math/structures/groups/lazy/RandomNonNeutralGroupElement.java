package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

public class RandomNonNeutralGroupElement extends LazyGroupElement {
    public RandomNonNeutralGroupElement(LazyGroup group) {
        super(group);
    }

    @Override
    protected GroupElementImpl computeConcreteValue() {
        return group.impl.getUniformlyRandomNonNeutral();
    }
}
