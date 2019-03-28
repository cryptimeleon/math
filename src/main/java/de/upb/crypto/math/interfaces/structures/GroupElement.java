package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;
import de.upb.crypto.math.swante.MyExponentiationAlgorithms;

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
     * Computes this.op(result of given expression).
     *
     * @param expr a PowProductExpression
     * @return this.op(expr.evaluate ())
     * @throws IllegalArgumentException if an element is of the wrong type
     * @see Group::evaluate()
     */
    default GroupElement op(PowProductExpression expr) throws IllegalArgumentException {
        return this.op(getStructure().evaluate(expr));
    }
    
    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes this.inv().pow(-k)
     */
    default GroupElement pow(BigInteger k) { //default implementation: square&multiply algorithm
        // Todo: switch according to most efficient expo algo (cost of invert)
        return MyExponentiationAlgorithms.simpleSquareAndMultiplyPow(this, k);
    }
    
    /**
     * @return this element "squared" (if op is an multiplication), or "doubled" (if op is an addition)
     * If there is a more efficient algorithm for squaring (e.g. for elliptic curve points), these classes should override this method.
     */
    default GroupElement square() {
        return this.op(this);
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
     * Returns a new {@link PowProductExpression} containing exactly this group element.
     */
    default PowProductExpression asPowProductExpression() {
        return getStructure().powProductExpression().op(this);
    }
    
    
}
