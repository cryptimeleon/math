package org.cryptimeleon.math.structures.groups;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.basic.BasicGroup;
import org.cryptimeleon.math.structures.groups.basic.BasicGroupElement;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;

/**
 * Represents a group instantiated from either the additive or unit group of a ring.
 */
public class RingGroup extends BasicGroup {
    protected RingGroup(GroupImpl impl) {
        super(impl);
    }

    public RingGroup(Representation repr) {
        super(repr);
    }

    /**
     * Instantiates a {@code RingGroup} representing the additive group of the given ring.
     * @param ring the base ring
     * @return the additive group
     */
    public static RingGroup additiveGroupOf(Ring ring) {
        return new RingGroup(new RingAdditiveGroupImpl(ring));
    }

    /**
     * Instantiates a {@code RingGroup} representing the unit group of the given ring.
     * @param ring the base ring
     * @return the unit group
     */
    public static RingGroup unitGroupOf(Ring ring) {
        return new RingGroup(new RingUnitGroupImpl(ring));
    }

    public class RingGroupElement extends BasicGroupElement {
        public RingGroupElement(RingElement ringElement) {
            super(RingGroup.this, ((RingGroupImpl) RingGroup.this.impl).getElement(ringElement));
        }
    }

    /**
     * Constructs a {@code RingGroupElement} from the given {@code RingElement} for use in {@code RingGroup}s.
     * @param elem the ring element to convert
     * @return the corresponding {@code RingGroupElement}
     */
    public RingGroupElement getElement(RingElement elem) {
        return new RingGroupElement(elem);
    }
}
