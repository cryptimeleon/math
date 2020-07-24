package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

import java.util.concurrent.ExecutionException;

public class ConstLazyGroupElement extends LazyGroupElement {
    public ConstLazyGroupElement(LazyGroup group, GroupElementImpl concreteValue) {
        super(group, concreteValue);
    }

    @Override
    protected GroupElementImpl computeConcreteValue() {
        try {
            return concreteValue.get();
        } catch (InterruptedException|ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
