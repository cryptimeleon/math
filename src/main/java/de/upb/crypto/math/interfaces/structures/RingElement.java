package de.upb.crypto.math.interfaces.structures;

import java.math.BigInteger;

/**
 * Immutable objects representing a Ring element
 */
public interface RingElement extends Element {
    @Override
    public Ring getStructure();

    /**
     * Interprets this element as an element of this R's unit group
     */
    default RingUnitGroup.RingUnitGroupElement toUnitGroupElement() {
        return new RingUnitGroup(getStructure()).new RingUnitGroupElement(this);
    }

    /**
     * Interprets this element as an element of this R's additive group
     */
    default RingAdditiveGroup.RingAdditiveGroupElement toAdditiveGroupElement() {
        return new RingAdditiveGroup(getStructure()).new RingAdditiveGroupElement(this);
    }

    /**
     * Computes this + e
     *
     * @param e the addend
     * @return the result
     */
    RingElement add(Element e);

    /**
     * Computes the additive inverse of this element
     *
     * @return the result
     */
    RingElement neg();

    /**
     * Computes this - e
     *
     * @param e the subtrahend
     * @return the result
     */
    default RingElement sub(Element e) {
        return add(((RingElement) e).neg());
    }

    /**
     * Computes this * e
     *
     * @param e the factor
     * @return the result
     */
    RingElement mul(Element e);

    /**
     * Computes this * k (equivalent to this + this + ... [k times])
     *
     * @param k the factor
     * @return the result
     */
    default RingElement mul(BigInteger k) { //default implementation: double&add algorithm
        if (k.signum() < 0)
            return mul(k.negate()).neg();
        RingElement result = getStructure().getZeroElement();
        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = result.add(result);
            if (k.testBit(i))
                result = result.add(this);
        }
        return result;
    }

    /**
     * Calculates this^k.
     * (Note that (anything)^0 = 1, particularly 0^0 = 1)
     */
    default RingElement pow(BigInteger k) { //default implementation: square&multiply algorithm
        if (k.signum() < 0)
            return pow(k.negate()).inv();
        RingElement result = getStructure().getOneElement();
        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = result.mul(result);
            if (k.testBit(i))
                result = result.mul(this);
        }
        return result;
    }

    /**
     * Computes the multiplicative inverse of this element
     *
     * @return the result
     * @throws UnsupportedOperationException if this is not a unit
     */
    RingElement inv() throws UnsupportedOperationException;

    /**
     * Predicate to determine whether an element has a multiplicative inverse.
     */
    default boolean isUnit() {
        try {
            inv();
        } catch (UnsupportedOperationException e) {
            return false;
        }
        return true;
    }

    /**
     * Computes this / e
     *
     * @param e the divisor
     * @return the result
     * @throws IllegalArgumentException if e is not a unit
     */
    default RingElement div(Element e) throws IllegalArgumentException {
        return mul(((RingElement) e).inv());
    }

    /**
     * Returns true iff there exists an x in the ring such that
     * this*x = e.
     *
     * @throws UnsupportedOperationException if this cannot be decided (efficiently)
     */
    boolean divides(RingElement e) throws UnsupportedOperationException;

    /**
     * Returns an array "result" such that
     * this = result[0]*e + result[1]
     * and
     * result[1].getRank() < e.getRank() or result[1] = 0
     * <p>
     * This definition implies that the remainder is zero if and only if e divides this element.
     * ("Only if" is clear. "If" follows because e divides result[1] and hence result[1].getRank() >= e.getRank(), which contradicts result[1].getRank() < e.getRank())
     *
     * @throws UnsupportedOperationException If the ring is not a euclidean domain
     * @throws IllegalArgumentException      if e is zero
     */
    RingElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException;

    /**
     * Implements the euclidean function of a euclidean domain, i.e.
     * a) rank(a) >= 0 for any a
     * b) rank(ab) >= rank(a) for any a,b != 0
     * c) the remainder rank after divideWithRemainder is less than the divisor's rank (see divideWithRemainder)
     * <p>
     * (for example, this corresponds to degrees for polynomials with polynomial division)
     * the rank of the zero element is undefined (no guarantee as to what this method returns in that case)
     *
     * @throws UnsupportedOperationException if the ring is not a euclidean domain
     */
    BigInteger getRank() throws UnsupportedOperationException;

    /**
     * Returns true iff this is the zero element of the ring.
     */
    default boolean isZero() {
        return this.equals(getStructure().getZeroElement());
    }

    /**
     * Returns true iff this is the one of the ring.
     */
    default boolean isOne() {
        return this.equals(getStructure().getOneElement());
    }

    public default RingElement square() {
        return this.mul(this);
    }
}
