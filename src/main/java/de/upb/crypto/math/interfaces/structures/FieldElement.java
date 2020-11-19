package de.upb.crypto.math.interfaces.structures;

import java.math.BigInteger;

/**
 * Immutable objects representing a field element.
 */
public interface FieldElement extends RingElement {
    @Override
    public FieldElement add(Element e);

    @Override
    public FieldElement neg();

    @Override
    public default FieldElement sub(Element e) {
        return (FieldElement) RingElement.super.sub(e);
    }

    @Override
    public FieldElement mul(Element e);

    @Override
    public default FieldElement mul(BigInteger k) {
        return (FieldElement) RingElement.super.mul(k);
    }

    @Override
    public default FieldElement mul(long k) {
        return mul(BigInteger.valueOf(k));
    }

    @Override
    public default FieldElement pow(BigInteger k) {
        return (FieldElement) RingElement.super.pow(k);
    }

    @Override
    public default FieldElement pow(long k) {
        return pow(BigInteger.valueOf(k));
    }

    @Override
    public FieldElement inv() throws UnsupportedOperationException;

    @Override
    public default FieldElement div(Element e) throws IllegalArgumentException {
        return (FieldElement) RingElement.super.div(e);
    }

    @Override
    default FieldElement square() {
        return (FieldElement) RingElement.super.square();
    }

    @Override
    public Field getStructure();

    @Override
    public default boolean divides(RingElement e) throws UnsupportedOperationException {
        return this.isZero() == e.isZero();
    }

    @Override
    public default RingElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException {
        if (e.isZero())
            throw new IllegalArgumentException("Division by zero");
        return new FieldElement[]{this.div(e), getStructure().getZeroElement()};
    }

    @Override
    public default BigInteger getRank() throws UnsupportedOperationException {
        return BigInteger.ZERO;
    }

}
