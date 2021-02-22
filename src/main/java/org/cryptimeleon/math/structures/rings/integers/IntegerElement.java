package org.cryptimeleon.math.structures.rings.integers;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.annotations.AnnotatedUbrUtil;
import org.cryptimeleon.math.hash.annotations.UniqueByteRepresented;
import org.cryptimeleon.math.serialization.BigIntegerRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.Element;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;

import java.math.BigInteger;
import java.util.Objects;

/**
 * An Integer (as an Element of {@link IntegerRing}).
 */
public class IntegerElement implements RingElement {
    private static final IntegerRing ring = new IntegerRing();

    @UniqueByteRepresented
    private final BigInteger v;

    public IntegerElement(BigInteger v) {
        this.v = v;
    }

    public IntegerElement(long v) {
        this.v = BigInteger.valueOf(v);
    }

    @Override
    public Representation getRepresentation() {
        return new BigIntegerRepresentation(v);
    }

    /**
     * Returns the {@code BigInteger} underlying this element.
     */
    public BigInteger getBigInt() {
        return v;
    }

    @Override
    public Ring getStructure() {
        return ring;
    }

    @Override
    public IntegerElement add(Element e) {
        return new IntegerElement(v.add(((IntegerElement) e).v));
    }

    @Override
    public IntegerElement neg() {
        return new IntegerElement(v.negate());
    }

    @Override
    public IntegerElement mul(Element e) {
        return new IntegerElement(v.multiply(((IntegerElement) e).v));
    }

    @Override
    public IntegerElement mul(BigInteger k) {
        return new IntegerElement(v.multiply(k));
    }

    @Override
    public IntegerElement inv() throws UnsupportedOperationException {
        if (v.abs().equals(BigInteger.ONE))
            return this;
        throw new UnsupportedOperationException(this + " has no inverse");
    }

    @Override
    public boolean divides(RingElement e) throws UnsupportedOperationException {
        BigInteger ev = ((IntegerElement) e).v;
        return ev.remainder(v).equals(BigInteger.ZERO);
    }

    @Override
    public IntegerElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException {
        if (e.isZero())
            throw new IllegalArgumentException("division by zero");
        BigInteger[] res = v.divideAndRemainder(((IntegerElement) e).v);
        return new IntegerElement[]{new IntegerElement(res[0]), new IntegerElement(res[1])};
    }

    @Override
    public BigInteger getRank() throws UnsupportedOperationException {
        return v.abs();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerElement other = (IntegerElement) o;
        return Objects.equals(v, other.v);
    }

    @Override
    public int hashCode() {
        return v.hashCode();
    }

    @Override
    public String toString() {
        return v.toString();
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return AnnotatedUbrUtil.autoAccumulate(accumulator, this);
    }

    @Override
    public BigInteger asInteger() throws UnsupportedOperationException {
        return v;
    }
}
