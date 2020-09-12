package de.upb.crypto.math.interfaces.structures.group.impl;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Allows interpreting a ring as an additive group.
 */
public class RingAdditiveGroupImpl extends RingGroupImpl {

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
     * Note that such an element is not equal (according to equals()) to its original ring element.
     * Use projectToRing() to interpret the element as a ring element again.
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
