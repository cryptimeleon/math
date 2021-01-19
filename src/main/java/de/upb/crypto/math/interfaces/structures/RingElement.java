package de.upb.crypto.math.interfaces.structures;

import java.math.BigInteger;

/**
 * Immutable objects representing a ring element.
 */
public interface RingElement extends Element {
    @Override
    Ring getStructure();

    /**
     * Interprets this element as an element of this ring's unit group.
     */
    default GroupElement toUnitGroupElement() {
        return RingGroup.unitGroupOf(getStructure()).getElement(this);
    }

    /**
     * Interprets this element as an element of this rings additive group.
     */
    default GroupElement toAdditiveGroupElement() {
        return RingGroup.additiveGroupOf(getStructure()).getElement(this);
    }

    /**
     * Computes \(\text{this} + e\).
     *
     * @param e the addend
     * @return the result
     */
    RingElement add(Element e);

    /**
     * Computes the additive inverse of this element.
     *
     * @return the result
     */
    RingElement neg();

    /**
     * Computes \(\text{this} - e\).
     *
     * @param e the subtrahend
     * @return the result
     */
    default RingElement sub(Element e) {
        return add(((RingElement) e).neg());
    }

    /**
     * Computes \(\text{this} \cdot e\).
     *
     * @param e the factor
     * @return the result
     */
    RingElement mul(Element e);

    /**
     * Computes \(\text{this} \cdot k\) (equivalent to \(\text{this} + \text{this} + \cdots\) k-times).
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
     * Computes \(\text{this} \cdot k\) (equivalent to \(\text{this} + \text{this} + \cdots\) k-times).
     *
     * @param k the factor
     * @return the result
     */
    default RingElement mul(long k) {
        return mul(BigInteger.valueOf(k));
    }

    /**
     * Calculates \(\text{this}^k\).
     * <p>
     * Note that \(a^0 = 1\) for any \(a\) in the ring, particularly \(0^0 = 1\).
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
     * Calculates \(\text{this}^k\).
     * <p>
     * Note that \(a^0 = 1\) for any \(a\) in the ring, particularly \(0^0 = 1\).
     */
    default RingElement pow(long k) {
        return pow(BigInteger.valueOf(k));
    }

    /**
     * Computes the multiplicative inverse of this element.
     *
     * @return the result
     * @throws UnsupportedOperationException if this is not a unit
     */
    RingElement inv() throws UnsupportedOperationException;

    /**
     * Determines whether an element has a multiplicative inverse.
     *
     * @return true if the element has a multiplicative inverse, else false.
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
     * Computes \(\text{this} / e = \text{this} \cdot e^{-1}\).
     *
     * @param e the divisor
     * @return the result
     * @throws IllegalArgumentException if e is not a unit
     */
    default RingElement div(Element e) throws IllegalArgumentException {
        return mul(((RingElement) e).inv());
    }

    /**
     * Returns true iff there exists an \(x\) in the ring such that \(\text{this} \cdot x = e\).
     *
     * @throws UnsupportedOperationException if this cannot be decided (efficiently)
     */
    boolean divides(RingElement e) throws UnsupportedOperationException;

    /**
     * Divides this by e with remainder, returning both quotient and remainder.
     * <p>
     * Specifically, returns an array {@code result} such that the first entry contains the quotient
     * and the second entry the remainder.
     * Furthermore, {@code result[1].getRank() < e.getRank() or result[1] = 0}.
     * <p>
     * This definition implies that the remainder is zero if and only if e divides this element.
     *
     * @throws UnsupportedOperationException if the ring is not a euclidean domain
     * @throws IllegalArgumentException      if e is zero
     */
    RingElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException;

    /**
     * Implements the euclidean function of a euclidean domain.
     * <p>
     * The euclidean function is a function from \(R \setminus \{0\}\) to \(\mathbb{N}_0\) such that
     * <ul>
     * <li>{@code a.getRank() >= 0} for any {@code a} in the ring
     * <li>{@code a.mul(b).getRank() >= a.getRank()} for any {@code a, b != 0}
     * <li>the remainder rank after {@code divideWithRemainder} is less than the divisor's rank
     * </ul>
     * For example, for a polynomial ring this corresponds to the degree of the polynomial.
     * <p>
     * The rank of the zero element is undefined (no guarantee as to what this method returns in that case).
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
     * Returns true iff this is the one element of the ring.
     */
    default boolean isOne() {
        return this.equals(getStructure().getOneElement());
    }

    /**
     * Computes \(\text{this}^2\).
     * <p>
     * Useful if the ring allows squaring to be more efficiently implemented than general exponentiation.
     */
    default RingElement square() {
        return this.mul(this);
    }

    /**
     * Interprets this element as an integer.
     * <p>
     * Formally, this method shall return the inverse of {@link Ring#getElement(BigInteger)}, i.e.
     * {@code x.getStructure().getElement(x.asInteger()).equals(x)} (if {@code asInteger()} doesn't throw an exception).
     *
     * @return the integer corresponding to this element
     * @throws UnsupportedOperationException if no such element exists or cannot be efficiently computed
     */
    default BigInteger asInteger() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot interpret "+getClass().getName()+" as an integer");
    }
}
