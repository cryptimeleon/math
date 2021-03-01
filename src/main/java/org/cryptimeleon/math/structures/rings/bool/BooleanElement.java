package org.cryptimeleon.math.structures.rings.bool;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.serialization.ByteArrayRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.Element;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;

import java.math.BigInteger;

/**
 * Element of {@link BooleanStructure}.
 */
public class BooleanElement implements RingElement {
    /**
     * The Boolean value represented by this {@code BooleanElement}.
     */
    protected boolean value;

    /**
     * The {@code BooleanElement} corresponding to a {@code true} value.
     */
    public static BooleanElement TRUE = new BooleanElement(true);
    /**
     * The {@code BooleanElement} corresponding to a {@code false} value.
     */
    public static BooleanElement FALSE = new BooleanElement(false);

    public BooleanElement(boolean value) {
        this.value = value;
    }

    public BooleanElement(Representation repr) {
        this(repr.bytes().get()[0] != 0);
    }

    @Override
    public Ring getStructure() {
        return new BooleanStructure();
    }

    /**
     * Returns result of calling Boolean XOR on this value and the argument.
     */
    @Override
    public RingElement add(Element e) {
        return ((BooleanElement) e).value != this.value ? TRUE : FALSE;
    }

    /**
     * Negates this Boolean value.
     * @return {@code TRUE} if this value is {@code FALSE}, else {@code FALSE}
     */
    @Override
    public RingElement neg() {
        return value ? FALSE : TRUE;
    }

    /**
     * Returns result of calling Boolean AND on this value and the argument.
     */
    @Override
    public RingElement mul(Element e) {
        return ((BooleanElement) e).value && this.value ? TRUE : FALSE;
    }

    /**
     * If this value is {@code TRUE}, returns {@code TRUE}; otherwise throws a {@code UnsupportedOperationException}.
     * <p>
     * {@code FALSE} is the zero element and therefore has no multiplicative inversion.
     * @throws UnsupportedOperationException if this value is {@code FALSE}
     */
    @Override
    public RingElement inv() throws UnsupportedOperationException {
        if (!value)
            throw new UnsupportedOperationException("Cannot invert FALSE");
        return TRUE;
    }

    /**
     * Returns result of calling Boolean OR on this value and the argument.
     */
    public BooleanElement or(Element e) {
        return ((BooleanElement) e).value || value ? TRUE : FALSE;
    }

    @Override
    public boolean divides(RingElement e) throws UnsupportedOperationException {
        return value || !((BooleanElement) e).value;
    }

    @Override
    public RingElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented yet"); //TODO
    }

    @Override
    public BigInteger getRank() throws UnsupportedOperationException {
        return BigInteger.ONE;
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        accumulator.append(value ? 1 : 0);
        return accumulator;
    }

    @Override
    public int hashCode() {
        return value ? 1 : 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BooleanElement && ((BooleanElement) obj).value == this.value;
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    @Override
    public Representation getRepresentation() {
        return new ByteArrayRepresentation(new byte[] {(byte) (value ? 1 : 0)});
    }

    @Override
    public BigInteger asInteger() throws UnsupportedOperationException {
        return value ? BigInteger.ONE : BigInteger.ZERO;
    }
}
