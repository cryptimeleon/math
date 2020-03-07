package de.upb.crypto.math.structures.integers;

import de.upb.crypto.math.hash.annotations.AnnotatedUbrUtil;
import de.upb.crypto.math.hash.annotations.UniqueByteRepresented;
import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.BigIntegerRepresentation;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

/**
 * An Integer (as an Element of IntegerRing)
 */
public class IntegerElement implements RingElement {
    private static IntegerRing ring = new IntegerRing();

    @UniqueByteRepresented
    private BigInteger v;

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
    public boolean equals(Object obj) {
        return obj instanceof IntegerElement && v.equals(((IntegerElement) obj).v);
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
}
