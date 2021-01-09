package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.group.GroupElementConstantExpr;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.structures.cartesian.GroupElementVector;
import de.upb.crypto.math.structures.cartesian.Vector;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.math.BigInteger;

/**
 * Immutable objects representing elements of a group.
 *
 * Potentially calls here may return immediately and not block.
 * For example, g.op(h) may immediately return an object representing the
 * result of the group operation applied to g and h. This object is usable as such.
 * Internally, however, the actual computation of the group operation may be deferred
 * until the value is really needed.
 * This has performance advantages, for example, we can use multiexponentation
 * algorithms for computations of values like g.pow(x).op(h.pow(y)).
 *
 * You can (but don't have to) call compute() on a group element.
 * This will start computing its actual value asynchronously in the background.
 * Example (ElGamal encryption):
 * c0 = g.pow(r).compute(); //will return immediately, but compute g^r in the background.
 * c1 = h.pow(r).op(m).compute(); //will also start computing (in parallel)
 *
 * c0.getRepresentation(); //will block until the value of c0 is ready.
 * c1.getRepresentation();  //will block until the value of c1 is ready.
 *
 * Without the compute() calls, the example still produces the same output,
 * but c0 and c1 will be computed sequentially.
 *
 * <p>
 * Implementations must properly implement equals() and hashCode()
 */
public interface GroupElement extends Element, UniqueByteRepresentable {
    @Override
    Group getStructure();

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

    default GroupElementExpression op(GroupElementExpression e) {
        return expr().op(e);
    }

    default GroupElementExpression op(String variable) {
        return expr().op(variable);
    }

    /**
     * @return this element "squared" (if op is an multiplication), or "doubled" (if op is addition)
     */
    default GroupElement square() {
        return this.op(this);
    }

    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes this.inv().pow(-k).
     */
    GroupElement pow(BigInteger exponent);

    /**
     * Calculates the result of applying the group operation k times.
     * Note that this is only well-defined if k is from Zn, such that getStructure().size() divides n.
     */
    default GroupElement pow(ZnElement k) {
        return pow((RingElement) k);
    }

    /**
     * Calculates the result of applying the group operation k times.
     * This is only well-defined if this.getStructure().size() divides k.getStructure().getCharacteristic()
     * and k.asInteger() doesn't throw an exception.
     */
    default GroupElement pow(RingElement k) {
        if (!getStructure().size().equals(k.getStructure().getCharacteristic())
                && !k.getStructure().getCharacteristic().equals(BigInteger.ZERO)
                && !getStructure().size().mod(k.getStructure().getCharacteristic()).equals(BigInteger.ZERO))
            throw new IllegalArgumentException("Cannot raise to the power of "+k);
        return pow(k.asInteger());
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
     * Computes vector (g^exponents[0], g^exponents[1], ...)
     * @param exponents the exponents to use
     * @return (g^exponents[0], g^exponents[1], ...)
     */
    default GroupElementVector pow(Vector<? extends RingElement> exponents) {
        return GroupElementVector.generate(i -> this, exponents.length()).pow(exponents);
    }

    default GroupElementExpression pow(ExponentExpr exponent) {
        return expr().pow(exponent);
    }

    default GroupElementExpression pow(String variable) {
        return expr().pow(variable);
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
    default GroupElementConstantExpr expr() {
        return new GroupElementConstantExpr(this);
    }

    /**
     * Advises the GroupElement to prepare it for later pow() calls.
     * This will take some time and should only be done ahead of time.
     * That is, the usual usage pattern should be:
     *
     * //Setting up your encryption scheme (or whatever)
     * GroupElement g = group.getUniformlyRandomElement().precomputPow();
     * //Then (maybe even multiple) future calls of
     * GroupElement encrypt(GroupElement m) {
     *     return m.op(g.pow(sk)).compute();
     * }
     * Don't use
     * g.precomputePow().pow(x).compute();
     * unless you're planning to do more exponentiations of g in the future.
     *
     * Uses a reasonable default for the memory consumed by this. Use precomputePow(int) to customize.
     *
     * @return the same object (for chaining calls)
     */
    default GroupElement precomputePow() {
        precomputePow(8);
        return this;
    }

    /**
     * Advises the GroupElement to prepare it for later pow() calls.
     * This will take some time and should only be done ahead of time.
     * That is, the usual usage pattern should be:
     *
     * //Setting up your encryption scheme (or whatever)
     * GroupElement g = group.getUniformlyRandomElement().precomputPow();
     * //Then (maybe even multiple) future calls of
     * GroupElement encrypt(GroupElement m) {
     *     return m.op(g.pow(sk)).compute();
     * }
     * Don't use
     * g.precomputePow().pow(x).compute();
     * unless you're planning to do more exponentiations of g in the future.
     *
     * @param windowSize an indicator for how much memory you're willing to invest. Precomputation will take up space of roughly 2^(windowSize-1) group elements.
     * @return the same object (for chaining calls)
     */
    GroupElement precomputePow(int windowSize);

    /**
     * Hint that the concrete value of this GroupElement will be accessed soon (e.g., via getRepresentation() or equals()).
     * Will start computing stuff in the background.
     * @return the same object (for chaining calls)
     */
    GroupElement compute();

    /**
     * Will compute stuff synchronously (this call blocks) so that the next call
     * requiring the concrete value of this group element can immediately retrieve it.
     * For designers of cryptographic schemes, were should be no reason to call this. Instead, use compute(),
     * which does the same, but asynchronously (i.e. concurrently).
     *
     * @return the same object (for chaining calls)
     */
    GroupElement computeSync();

    /**
     * Returns true if a concrete value has already been computed.
     * No need to call this outside of upb.crypto.math, it's meant to allow for optimizations.
     */
    boolean isComputed();
}
