package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Allows interpreting a ring as its unit group (i.e. the set of elements with a multiplicative inverse, with the ring multiplication).
 */
public class RingUnitGroup extends RingGroup {
    public RingUnitGroup(Ring ring) {
        super(ring);
    }

    public RingUnitGroup(Representation repr) {
        super(repr);
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return ring.sizeUnitGroup();
    }

    @Override
    public RingUnitGroupElement getNeutralElement() {
        return new RingUnitGroupElement(ring.getOneElement());
    }

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return new RingUnitGroupElement(ring.getUniformlyRandomUnit());
    }

    /**
     * A ring element interpreted as an element of the ring's unit group.
     * Note that such an element is not equal (according to equals()) to its original ring element.
     * Use projectToRing() to interpret the element as a ring element again.
     * <p>
     * Also, note that you could potentially wrap non-units in a RingUniGroupElement.
     * This is not checked (but obviously discouraged). The invert operation will fail.
     */
    public class RingUnitGroupElement extends RingGroupElement {
        public RingUnitGroupElement(RingElement e) {
            super(e);
        }

        @Override
        public RingUnitGroupElement op(Element e) {
            return new RingUnitGroupElement(element.mul(((RingUnitGroupElement) e).element));
        }

        @Override
        public RingUnitGroup getStructure() {
            return RingUnitGroup.this;
        }

        @Override
        public RingUnitGroupElement inv() {
            return new RingUnitGroupElement(element.inv());
        }

        @Override
        public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
            return element.updateAccumulator(accumulator);
        }

    }

    @Override
    public RingUnitGroupElement getElement(Representation repr) {
        return new RingUnitGroupElement(ring.getElement(repr));
    }

    @Override
    public GroupElement getGenerator() throws UnsupportedOperationException {
        if (ring instanceof Field)
            return new RingUnitGroupElement(((Field) ring).getPrimitiveElement());
        throw new UnsupportedOperationException("Cannot compute generator for this ring group " + this);
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return ring.getUniqueByteLength();
    }

    @Override
    public boolean isCommutative() {
        return ring.isCommutative();
    }

    @Override
    public int estimateCostOfInvert() {
        return 100; //no information. Let's say it costs as much as a group op.
    }
}
