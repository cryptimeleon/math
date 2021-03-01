package org.cryptimeleon.math.structures.groups;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.rings.Field;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Allows interpreting a ring as its unit group.
 * <p>
 * The unit group consists of the set of elements with a multiplicative inverse.
 * The group operation is multiplication and the neutral element is called the one element.
 */
public class RingUnitGroupImpl extends RingGroupImpl {
    public RingUnitGroupImpl(Ring ring) {
        super(ring);
    }

    public RingUnitGroupImpl(Representation repr) {
        super(repr);
    }

    @Override
    public RingGroupElementImpl getElement(RingElement ringElement) {
        return new RingUnitGroupElementImpl(ringElement);
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return ring.sizeUnitGroup();
    }

    @Override
    public boolean hasPrimeSize() {
        return size().isProbablePrime(128);
    }

    @Override
    public double estimateCostInvPerOp() {
        // Does not really work here since the numbers depend on the exact ring
        // Used Zn(2^128) here
        return 0.1;
    }

    @Override
    public RingUnitGroupElementImpl getNeutralElement() {
        return new RingUnitGroupElementImpl(ring.getOneElement());
    }

    @Override
    public RingUnitGroupElementImpl getUniformlyRandomElement() throws UnsupportedOperationException {
        return new RingUnitGroupElementImpl(ring.getUniformlyRandomUnit());
    }

    /**
     * A ring element interpreted as an element of the ring's unit group.
     * Note that such an element is not equal (according to equals()) to its original ring element.
     * Use projectToRing() to interpret the element as a ring element again.
     * <p>
     * Also, note that you could potentially wrap non-units in a RingUniGroupElement.
     * This is not checked (but obviously discouraged). The invert operation will fail.
     */
    public class RingUnitGroupElementImpl extends RingGroupElementImpl {
        public RingUnitGroupElementImpl(RingElement e) {
            super(e);
        }

        @Override
        public RingUnitGroupElementImpl op(GroupElementImpl e) {
            return new RingUnitGroupElementImpl(element.mul(((RingUnitGroupElementImpl) e).element));
        }

        @Override
        public RingUnitGroupElementImpl pow(BigInteger exponent) {
            return new RingUnitGroupElementImpl(element.pow(exponent));
        }

        @Override
        public RingUnitGroupImpl getStructure() {
            return RingUnitGroupImpl.this;
        }

        @Override
        public RingUnitGroupElementImpl inv() {
            return new RingUnitGroupElementImpl(element.inv());
        }

        @Override
        public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
            return element.updateAccumulator(accumulator);
        }

    }

    @Override
    public RingUnitGroupElementImpl restoreElement(Representation repr) {
        return new RingUnitGroupElementImpl(ring.restoreElement(repr));
    }

    @Override
    public GroupElementImpl getGenerator() throws UnsupportedOperationException {
        if (ring instanceof Field)
            return new RingUnitGroupElementImpl(((Field) ring).getPrimitiveElement());
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
}
