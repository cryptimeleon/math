package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.expressions.group.GroupElementLiteralExpr;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.math.BigInteger;

/**
 * Immutable objects representing elements of a group.
 * <p>
 * Implementations must properly implement equals() and hashCode()
 */
public interface GroupElement extends Element, UniqueByteRepresentable {
    @Override
    public Group getStructure();

    /**
     * Calculates the inverse of this group element
     *
     * @return an element x such that x.op(this).equals(getStructure().getNeutralElement())
     */
    GroupElement inv();

    /**
     * Calculates the result of e op this.
     *
     * @param e right hand side of the operation
     * @return the element resulting from the group operation
     * @throws IllegalArgumentException if e is of the wrong type
     */
    GroupElement op(Element e) throws IllegalArgumentException;

    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes this.inv().pow(-k)
     */
    default GroupElement pow(BigInteger k) { //default implementation: square&multiply algorithm
        if (k.signum() < 0)
            return pow(k.negate()).inv();
        GroupElement operand = this;

        GroupElement result = getStructure().getNeutralElement();
        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = result.op(result);
            if (k.testBit(i))
                result = result.op(operand);
        }
        return result;
    }

    /**
     * Calculates the result of applying the group operation k times.
     * Note that this is only well-defined if k is from Zn, such that getStructure().size() divides n.
     */
    default GroupElement pow(ZnElement k) {
        return pow(k.getInteger());
    }

    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes this.inv().pow(-k).
     * <p>
     * The caller should be aware that usually, exponents for large groups will not usually
     * fit into a long value (use pow(BigInteger) or pow(ZnElement) if your exponent is large).
     */
    default GroupElement pow(long k) {
        return pow(BigInteger.valueOf(k));
    }

    /**
     * Returns true iff this is the neutral element of the group.
     */
    default boolean isNeutralElement() {
        return this.equals(getStructure().getNeutralElement());
    }

    /**
     * Returns a {@link de.upb.crypto.math.expressions.group.GroupElementExpression} containing exactly this group element.
     */
    default GroupElementLiteralExpr expr() {
        return new GroupElementLiteralExpr(this);
    }
}
