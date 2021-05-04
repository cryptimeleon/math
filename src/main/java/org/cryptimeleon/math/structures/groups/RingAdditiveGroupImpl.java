package org.cryptimeleon.math.structures.groups;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Allows interpreting a ring as its additive group.
 */
class RingAdditiveGroupImpl extends RingGroupImpl {

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
        return ring.estimateCostNegPerOp();
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
    public RingAdditiveGroupElementImpl restoreElement(Representation repr) {
        return new RingAdditiveGroupElementImpl(ring.restoreElement(repr));
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
