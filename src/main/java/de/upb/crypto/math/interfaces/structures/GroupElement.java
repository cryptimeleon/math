package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
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
     * Should be called once before performing exponentiations with this
     * variable as base.
     * Useful for normalizing e.g. CurvePoints, thereby making add operations faster.
     * @param exponent the exponent that will be used for pow. Only if it is reasonably
     *                 large, the precomputations will actually be performed. If you
     *                 don't know the exact value for the exponent, but would like to
     *                 perform the precomputations no matter what it will eventually be,
     *                 you can pass null here.
     * @return returns the prepared GroupElement (should be reassigned to your local
     * base variable)
     */
    default GroupElement prepareForPow(BigInteger exponent) {return this;}
    
    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes this.inv().pow(-k)
     */
    default GroupElement pow(BigInteger k) { //default implementation: square&multiply algorithm
        return MyExponentiationAlgorithms.defaultPowImplementation(this, k);
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
