package de.upb.crypto.math.interfaces.structures;

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
     * Returns a new {@link PowProductExpression} containing exactly this group element.
     */
    default PowProductExpression asPowProductExpression() {
        return getStructure().powProductExpression().op(this);
    }
    
    /**
     * Precomputes the small powers of this element. Should ideally not be called twice
     * on the same instance. You should cache the result.
     * You can also override this method to accomplish that caching
     *
     * @param windowSize
     * @return array with x^1,x^3,x^5,...,x^(2^windowSize-1), assuming op is a multiplication
     */
    default GroupElement[] precomputePowersForSlidingWindow(int windowSize) {
        GroupElement[] res = new GroupElement[(1 << windowSize - 1)];
        GroupElement xx = this.op(this);
        GroupElement xPower = this;
        for (int i = 0; i < res.length; i++) {
            res[i] = xPower;
            xPower = xPower.op(xx);
        }
        return res;
    }
    
    /**
     * @param exponent
     * @param windowSize
     * @param smallPowersOfThis: the result of above method
     * @return this^exponent (assuming op is a multiplication), or this*exponent (if op is a addition), using the efficient sliding window technique
     */
    default GroupElement powUsingSlidingWindow(BigInteger exponent, int windowSize, GroupElement[] smallPowersOfThis) {
        GroupElement y = getStructure().getNeutralElement();
        int l = exponent.bitLength();
        int i = l - 1;
        if (windowSize > 20) {
            throw new IllegalArgumentException("too large windowSize");
        }
        while (i > -1) {
            if (exponent.testBit(i)) {
                int s = Math.max(0, i - windowSize + 1);
                int smallExponent = 0;
                while (!exponent.testBit(s)) {
                    s++;
                }
                for (int h = s; h <= i; h++) {
                    y = y.op(y);
                    if (exponent.testBit(h)) {
                        smallExponent += 1 << h - s;
                    }
                }
                
                y = y.op(smallPowersOfThis[smallExponent / 2]);
                i = s - 1;
            } else {
                y = y.op(y);
                i--;
            }
        }
        return y;
    }
}
