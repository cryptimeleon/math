package de.upb.crypto.math.structures.bool;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.ByteArrayRepresentation;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

public class BooleanElement implements RingElement {
    protected boolean value;

    public static BooleanElement TRUE = new BooleanElement(true);
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
     * Returns result of calling Boolean XOR on the two bits.
     */
    @Override
    public RingElement add(Element e) {
        return ((BooleanElement) e).value != this.value ? TRUE : FALSE;
    }

    @Override
    public RingElement neg() {
        return value ? FALSE : TRUE;
    }

    /**
     * Returns result of calling Boolean AND on the two bits.
     */
    @Override
    public RingElement mul(Element e) {
        return ((BooleanElement) e).value && this.value ? TRUE : FALSE;
    }

    @Override
    public RingElement inv() throws UnsupportedOperationException {
        if (!value)
            throw new UnsupportedOperationException("Cannot invert FALSE");
        return TRUE;
    }

    public BooleanElement or(Element e) {
        return ((BooleanElement) e).value || value ? TRUE : FALSE;
    }

    @Override
    public boolean divides(RingElement e) throws UnsupportedOperationException {
        return value || !((BooleanElement) e).value;
    }

    @Override
    public RingElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException {
        return new RingElement[] {}; //TODO
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
}
