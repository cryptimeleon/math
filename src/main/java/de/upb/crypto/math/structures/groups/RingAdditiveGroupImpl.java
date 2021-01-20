package de.upb.crypto.math.structures.groups;

import de.upb.crypto.math.hash.ByteAccumulator;
import de.upb.crypto.math.structures.rings.Ring;
import de.upb.crypto.math.structures.rings.RingElement;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Allows interpreting a ring as its additive group.
 */
public class RingAdditiveGroupImpl extends RingGroupImpl {

    /**
     * Instantiates this ring additive group.
     * @param ring the base ring to use
     */
    public RingAdditiveGroupImpl(Ring ring) {
        super(ring);
    }

    public RingAdditiveGroupImpl(Representation repr) {
        super(repr);
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return ring.size();
    }

    @Override
    public boolean hasPrimeSize() {
        return ring.hasPrimeSize();
    }

    @Override
    public double estimateCostInvPerOp() {
        // Does not really work here since the numbers depend on the exact ring
        // Used Zn(2^128) here
        return 1;
    }

    @Override
    public RingAdditiveGroupElementImpl getNeutralElement() {
        return new RingAdditiveGroupElementImpl(ring.getZeroElement());
    }

    @Override
    public RingAdditiveGroupElementImpl getUniformlyRandomElement() throws UnsupportedOperationException {
        return new RingAdditiveGroupElementImpl(ring.getUniformlyRandomElement());
    }

    @Override
    public RingAdditiveGroupElementImpl getGenerator() throws UnsupportedOperationException {
        if (hasPrimeSize() || size().equals(ring.getCharacteristic()))
            return new RingAdditiveGroupElementImpl(ring.getOneElement());

        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RingAdditiveGroupImpl && this.ring.equals(((RingAdditiveGroupImpl) obj).ring);
    }

    @Override
    public int hashCode() {
        return ring.hashCode();
    }

    @Override
    public RingGroupElementImpl getElement(RingElement ringElement) {
        return new RingAdditiveGroupElementImpl(ringElement);
    }

    /**
     * A ring element interpreted as an element of the ring's additive group.
     * <p>
     * Note that such an element is not equal (according to {@code equals()}) to its original ring element.
     * Use {@code projectToRing()} to interpret the element as a ring element again.
     */
    public class RingAdditiveGroupElementImpl extends RingGroupElementImpl {
        public RingAdditiveGroupElementImpl(RingElement e) {
            super(e);
        }

        @Override
        public RingAdditiveGroupElementImpl op(GroupElementImpl e) {
            return new RingAdditiveGroupElementImpl(element.add(((RingAdditiveGroupElementImpl) e).element));
        }

        @Override
        public RingAdditiveGroupElementImpl pow(BigInteger exponent) {
            return new RingAdditiveGroupElementImpl(element.mul(exponent));
        }

        @Override
        public RingAdditiveGroupImpl getStructure() {
            return RingAdditiveGroupImpl.this;
        }

        @Override
        public RingAdditiveGroupElementImpl inv() {
            return new RingAdditiveGroupElementImpl(element.neg());
        }

        @Override
        public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
            return element.updateAccumulator(accumulator);
        }

    }

    @Override
    public RingAdditiveGroupElementImpl getElement(Representation repr) {
        return new RingAdditiveGroupElementImpl(ring.getElement(repr));
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return ring.getUniqueByteLength();
    }

    @Override
    public boolean isCommutative() {
        return true;
    }
}
