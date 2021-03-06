package org.cryptimeleon.math.structures.rings;

import org.cryptimeleon.math.serialization.Representation;

import java.math.BigInteger;

/**
 * A commutative ring where every element except 0 has a multiplicative inverse.
 */
public interface Field extends Ring {
    /**
     * If this is a finite field, returns a generator of the field's unit group.
     * Repeated calls may or may not always return the same element.
     *
     * @throws UnsupportedOperationException if operation is not feasible or undefined
     */
    FieldElement getPrimitiveElement() throws UnsupportedOperationException;

    @Override
    default BigInteger sizeUnitGroup() {
        return size() == null ? null : size().subtract(BigInteger.ONE);
    }

    @Override
    FieldElement getZeroElement();

    @Override
    FieldElement getOneElement();

    @Override
    FieldElement restoreElement(Representation repr);

    @Override
    FieldElement getElement(BigInteger i);

    @Override
    default FieldElement getElement(long i) {
        return getElement(BigInteger.valueOf(i));
    }

    @Override
    FieldElement getUniformlyRandomElement() throws UnsupportedOperationException;

    @Override
    default FieldElement getUniformlyRandomUnit() throws UnsupportedOperationException {
        try {
            FieldElement result;
            do {
                result = getUniformlyRandomElement();
            } while (!result.isUnit());
            return result;
        } catch (RuntimeException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    default boolean isCommutative() {
        return true;
    }
}
