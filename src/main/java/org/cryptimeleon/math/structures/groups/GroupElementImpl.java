package org.cryptimeleon.math.structures.groups;

import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.Representable;

import java.math.BigInteger;

/**
 * Immutable objects representing elements of a group.
 * <p>
 * Usually wrapped by a {@link GroupElement} to offer additional evaluation capabilities.
 * <p>
 * Implementations must properly implement {@code equals()} and {@code hashCode()}.
 */
public interface GroupElementImpl extends Representable, UniqueByteRepresentable {
    /**
     * Retrieves the group this element belongs to.
     */
    GroupImpl getStructure();

    /**
     * Calculates the inverse of this group element.
     *
     * @return an element x such that {@code x.op(this).equals(getStructure().getNeutralElement())}
     */
    GroupElementImpl inv();

    /**
     * Calculates the result of this op e.
     *
     * @param e right hand side of the operation
     * @return the element resulting from the group operation
     * @throws IllegalArgumentException if e is of the wrong type
     */
    GroupElementImpl op(GroupElementImpl e) throws IllegalArgumentException;

    /**
     * Squares this if the group is multiplicative and doubles it if the group is additive.
     * <p>
     * If there is a more efficient algorithm for squaring (e.g. for elliptic curve points),
     * this method can be overwritten.
     *
     * @return this element "squared" (if op is an multiplication), or "doubled" (if op is an addition)
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
