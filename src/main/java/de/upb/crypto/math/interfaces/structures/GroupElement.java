package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.expressions.group.GroupElementConstantExpr;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.structures.cartesian.GroupElementVector;
import de.upb.crypto.math.structures.cartesian.RingElementVector;
import de.upb.crypto.math.structures.cartesian.Vector;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.math.BigInteger;

/**
 * Immutable objects representing elements of a group.
 * <p>
 * Potential calls here may return immediately and not block.
 * For example, {@code g.op(h)} may immediately return an object representing the
 * result of the group operation applied to {@code g} and {@code h}. This object is usable as such.
 * Internally, however, the actual computation of the group operation may be deferred
 * until the value is really needed.
 * This has performance advantages, for example, we can use multi-exponentiation
 * algorithms for computations of values like {@code g.pow(x).op(h.pow(y))}.
 * <p>
 * You can (but don't have to) call {@code compute()} on a group element.
 * This will start computing its actual value asynchronously in the background.
 * <p>
 * Example (ElGamal encryption):
 * <pre>
 * c0 = g.pow(r).compute(); // will return immediately, but compute g^r in the background.
 * c1 = h.pow(r).op(m).compute(); // will also start computing (in parallel)
 *
 * c0.getRepresentation(); // will block until the value of c0 is ready.
 * c1.getRepresentation(); // will block until the value of c1 is ready.
 * </pre>
 * Without the {@code compute()} calls, the example still produces the same output,
 * but {@code c0} and {@code c1} will be computed sequentially.
 * <p>
 * Implementations must properly implement {@code equals()} and {@code hashCode()}.
 */
public interface GroupElement extends Element, UniqueByteRepresentable {
    @Override
    Group getStructure();

    /**
     * Calculates the inverse of this group element.
     *
     * @return an element {@code x} such that {@code x.op(this).equals(getStructure().getNeutralElement())}
     */
    GroupElement inv();

    /**
     * Calculates the result of {@code this.op(e)}.
     *
     * @param e right hand side of the operation
     * @return the element resulting from the group operation
     * @throws IllegalArgumentException if e is of the wrong type
     */
    GroupElement op(Element e) throws IllegalArgumentException;

    /**
     * Computes {@code this.op(this)}.
     * <p>
     * Useful if this group allows squaring to be more efficiently implemented than general exponentiation as is the
     * case for elliptic curves.
     */
    default GroupElement square() {
        return this.op(this);
    }

    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes {@code this.inv().pow(-k)}.
     */
    GroupElement pow(BigInteger exponent);

    /**
     * Calculates the result of applying the group operation k times.
     * Note that this is only well-defined if k is from Zn,
     * such that {@code getStructure().size()} divides n.
     */
    default GroupElement pow(ZnElement k) {
        return pow(k.asExponent());
    }

    /**
     * Calculates the result of applying the group operation k times.
     * Note that this is only well-defined if k has an integer-like structure (e.g., Zn).
     */
    default GroupElement pow(RingElement k) {
        return pow(k.asExponent());
    }

    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes {@code this.inv().pow(-k)}.
     * <p>
     * The caller should be aware that usually, exponents for large groups will not usually
     * fit into a long value (use {@code pow(BigInteger)} or {@code pow(ZnElement)}
     * if your exponent is large).
     */
    default GroupElement pow(long k) {
        return pow(BigInteger.valueOf(k));
    }

    /**
     * Computes vector {@code (g.pow(exponents[0]), g.pow(exponents[1]), ...)}.
     * @param exponents the exponents to use (BigInteger, Long, or ZnElements)
     * @return {@code (g.pow(exponents[0]), g.pow(exponents[1]), ...)}
     */
    default GroupElementVector pow(Vector<? extends RingElement> exponents) {
        return GroupElementVector.generate(i -> this, exponents.length()).pow(exponents);
    }

    /**
     * Returns true iff this is the neutral element of the group.
     */
    default boolean isNeutralElement() {
        return this.equals(getStructure().getNeutralElement());
    }

    /**
     * Returns a {@link de.upb.crypto.math.expressions.group.GroupElementExpression}
     * containing exactly this group element.
     */
    default GroupElementConstantExpr expr() {
        return new GroupElementConstantExpr(this);
    }

    /**
     * Advises the {@code GroupElement} to prepare it for later {@code pow()} calls.
     * This will take some time and should only be done ahead of time.
     * That is, the usual usage pattern should be:
     * <pre>
     * //Setting up your encryption scheme (or whatever)
     * GroupElement g = group.getUniformlyRandomElement().precomputPow();
     * //Then (maybe even multiple) future calls of
     * GroupElement encrypt(GroupElement m) {
     *     return m.op(g.pow(sk)).compute();
     * }
     * </pre>
     * Don't use
     * <pre>
     * g.precomputePow().pow(x).compute();
     * </pre>
     * unless you're planning to do more exponentiations of {@code g} in the future.
     *
     * Uses a reasonable default for the memory consumed by this. Use {@code precomputePow(int)} to customize.
     *
     * @return the same object (for chaining calls)
     */
    default GroupElement precomputePow() {
        precomputePow(8);
        return this;
    }

    /**
     * Advises the {@code GroupElement} to prepare it for later {@code pow()} calls.
     * This will take some time and should only be done ahead of time.
     * That is, the usual usage pattern should be:
     * <pre>
     * //Setting up your encryption scheme (or whatever)
     * GroupElement g = group.getUniformlyRandomElement().precomputPow();
     * //Then (maybe even multiple) future calls of
     * GroupElement encrypt(GroupElement m) {
     *     return m.op(g.pow(sk)).compute();
     * }
     * </pre>
     * Don't use
     * <pre>
     * g.precomputePow().pow(x).compute();
     * </pre>
     * unless you're planning to do more exponentiations of {@code g} in the future.
     *
     * @param windowSize an indicator for how much memory you're willing to invest.
     *                   Precomputation will take up space of roughly 2^(windowSize-1) group elements
     * @return the same object (for chaining calls)
     */
    GroupElement precomputePow(int windowSize);

    /**
     * Hint that the concrete value of this GroupElement will be accessed soon
     * (e.g., via {@code getRepresentation()} or {@code equals()}). Will start computing stuff in the background.
     *
     * @return the same object (for chaining calls)
     */
    GroupElement compute();

    /**
     * Will compute stuff synchronously (this call blocks) so that the next call
     * requiring the concrete value of this group element can immediately retrieve it.
     * For designers of cryptographic schemes, were should be no reason to call this. Instead, use {@code compute()}
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
