package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Allows interpreting a ring as an additive group.
 */
public class RingAdditiveGroup extends RingGroup {

    public RingAdditiveGroup(Ring ring) {
        super(ring);
    }

    public RingAdditiveGroup(Representation repr) {
        super(repr);
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return ring.size();
    }

    @Override
    public RingAdditiveGroupElement getNeutralElement() {
        return new RingAdditiveGroupElement(ring.getZeroElement());
    }

    @Override
    public RingAdditiveGroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return new RingAdditiveGroupElement(ring.getUniformlyRandomElement());
    }

    @Override
    public GroupElement getGenerator() throws UnsupportedOperationException {
        if (size().isProbablePrime(1000) || size().equals(ring.getCharacteristic()))
            return new RingAdditiveGroupElement(ring.getOneElement());

        return super.getGenerator();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RingAdditiveGroup && this.ring.equals(((RingAdditiveGroup) obj).ring);
    }

    @Override
    public int hashCode() {
        return ring.hashCode();
    }

    /**
     * A ring element interpreted as an element of the ring's additive group.
     * Note that such an element is not equal (according to equals()) to its original ring element.
     * Use projectToRing() to interpret the element as a ring element again.
     */
    public class RingAdditiveGroupElement extends RingGroupElement {
        public RingAdditiveGroupElement(RingElement e) {
            super(e);
        }

        @Override
        public RingAdditiveGroupElement op(Element e) {
            return new RingAdditiveGroupElement(element.add(((RingAdditiveGroupElement) e).element));
        }

        @Override
        public RingAdditiveGroup getStructure() {
            return RingAdditiveGroup.this;
        }

        @Override
        public RingAdditiveGroupElement inv() {
            return new RingAdditiveGroupElement(element.neg());
        }

        @Override
        public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
            return element.updateAccumulator(accumulator);
        }

    }

    @Override
    public RingAdditiveGroupElement getElement(Representation repr) {
        return new RingAdditiveGroupElement(ring.getElement(repr));
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return ring.getUniqueByteLength();
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    @Override
    public int estimateCostOfInvert() {
        return 100; //no information. Let's say it's not more costly than addition
    }
}
