package org.cryptimeleon.math.structures.rings;

import org.cryptimeleon.math.structures.Element;

import java.math.BigInteger;

/**
 * Immutable objects representing a field element.
 */
public interface FieldElement extends RingElement {
    @Override
    FieldElement add(Element e);

    @Override
    FieldElement neg();

    @Override
    default FieldElement sub(Element e) {
        return (FieldElement) RingElement.super.sub(e);
    }

    @Override
    FieldElement mul(Element e);

    @Override
    default FieldElement mul(BigInteger k) {
        return (FieldElement) RingElement.super.mul(k);
    }

    @Override
    default FieldElement mul(long k) {
        return mul(BigInteger.valueOf(k));
    }

    @Override
    default FieldElement pow(BigInteger k) {
        return (FieldElement) RingElement.super.pow(k);
    }

    @Override
    default FieldElement pow(long k) {
        return pow(BigInteger.valueOf(k));
    }

    @Override
    FieldElement inv() throws UnsupportedOperationException;

    @Override
    default FieldElement div(Element e) throws IllegalArgumentException {
        return (FieldElement) RingElement.super.div(e);
    }

    @Override
    default FieldElement square() {
        return (FieldElement) RingElement.super.square();
    }

    @Override
    Field getStructure();

    @Override
    default boolean divides(RingElement e) throws UnsupportedOperationException {
        return this.isZero() == e.isZero();
    }

    @Override
    default RingElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException {
        if (e.isZero())
            throw new IllegalArgumentException("Division by zero");
        return new FieldElement[]{this.div(e), getStructure().getZeroElement()};
    }

    @Override
    default BigInteger getRank() throws UnsupportedOperationException {
        return BigInteger.ZERO;
    }

    /**
     * Computes this^characteristic.
     */
    default FieldElement applyFrobenius() {
        return this.pow(getStructure().getCharacteristic());
    }

    /**
     * Computes this^(characteristic^numberOfApplications)
     */
    default FieldElement applyFrobenius(int numberOfApplications) {
       FieldElement result = this;
        for (int i=0;i<numberOfApplications;i++)
            result = result.applyFrobenius();
        return result;
    }
}
