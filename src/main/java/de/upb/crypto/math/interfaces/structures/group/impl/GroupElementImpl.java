package de.upb.crypto.math.interfaces.structures.group.impl;

import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.serialization.Representable;

import java.math.BigInteger;

/**
 * Immutable objects representing elements of a group.
 * <p>
 * Implementations must properly implement equals() and hashCode()
 */
public interface GroupElementImpl extends Representable, UniqueByteRepresentable {
    GroupImpl getStructure();

    /**
     * Calculates the inverse of this group element
     *
     * @return an element x such that x.op(this).equals(getStructure().getNeutralElement())
     */
    GroupElementImpl inv();

    /**
     * Calculates the result of e op this.
     *
     * @param e right hand side of the operation
     * @return the element resulting from the group operation
     * @throws IllegalArgumentException if e is of the wrong type
     */
    GroupElementImpl op(GroupElementImpl e) throws IllegalArgumentException;

    /**
     * @return this element "squared" (if op is an multiplication), or "doubled" (if op is an addition)
     * If there is a more efficient algorithm for squaring (e.g. for elliptic curve points),
     * these classes should override this method.
     */
    default GroupElementImpl square() {
        return this.op(this);
    }

    /**
     * Calculates the result of applying the group operation k times.
     */
    default GroupElementImpl pow(BigInteger k) {
        if (k.signum() < 0)
            return pow(k.negate()).inv();
        GroupElementImpl operand = this;

        GroupElementImpl result = getStructure().getNeutralElement();
        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = result.op(result);
            if (k.testBit(i))
                result = result.op(operand);
        }
        return result;
    }

    /**
     * Returns true iff this is the neutral element of the group.
     */
    default boolean isNeutralElement() {
        return this.equals(getStructure().getNeutralElement());
    }
}
