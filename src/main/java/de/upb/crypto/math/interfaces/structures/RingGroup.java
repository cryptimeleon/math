package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.structures.group.impl.*;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.groups.basic.BasicGroup;
import de.upb.crypto.math.structures.groups.basic.BasicGroupElement;

public class RingGroup extends BasicGroup {
    protected RingGroup(GroupImpl impl) {
        super(impl);
    }

    public RingGroup(Representation repr) {
        super(repr);
    }

    public static RingGroup additiveGroupOf(Ring ring) {
        return new RingGroup(new RingAdditiveGroupImpl(ring));
    }

    public static RingGroup unitGroupOf(Ring ring) {
        return new RingGroup(new RingUnitGroupImpl(ring));
    }

    public class RingGroupElement extends BasicGroupElement {
        public RingGroupElement(RingElement ringElement) {
            super(RingGroup.this, ((RingGroupImpl) RingGroup.this.impl).getElement(ringElement));
        }
    }

    public RingGroupElement getElement(RingElement elem) {
        return new RingGroupElement(elem);
    }
}
