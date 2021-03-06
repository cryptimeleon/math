package org.cryptimeleon.math.structures.groups;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.StandaloneRepresentable;
import org.cryptimeleon.math.serialization.annotations.RepresentationRestorer;
import org.cryptimeleon.math.structures.Element;
import org.cryptimeleon.math.structures.groups.exp.Multiexponentiation;
import org.cryptimeleon.math.structures.groups.exp.SmallExponentPrecomputation;
import org.cryptimeleon.math.structures.rings.zn.Zp;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Optional;

/**
 * An algebraic group implementation which can be wrapped by a {@link Group} to offer additional evaluation capabilities.
 * Operations are defined on its elements.
 *
 * @see Group for the wrapper class
 * @see GroupElementImpl for the element class
 */
public interface GroupImpl extends StandaloneRepresentable, RepresentationRestorer {
    /**
     * Returns the neutral element of this group.
     */
    GroupElementImpl getNeutralElement();

    /**
     * Generates a uniformly random element of this group.
     * @throws UnsupportedOperationException if the random generation cannot be done
     */
    GroupElementImpl getUniformlyRandomElement() throws UnsupportedOperationException;

    /**
     * Generates a uniformly random non-neutral element of this group.
     * @throws UnsupportedOperationException if the random generation cannot be done
     */
    default GroupElementImpl getUniformlyRandomNonNeutral() throws UnsupportedOperationException {
        GroupElementImpl result;
        do {
            result = getUniformlyRandomElement();
        } while (result.isNeutralElement());

        return result;
    }

    /**
     * Restores a group element from its representation.
     */
    GroupElementImpl restoreElement(Representation repr);

    /**
     * Returns any generator of this group if the group is cyclic and it's feasible to compute a generator.
     * <p>
     * Repeated calls may or may not always supply the same generator again
     * (i.e. the output is not guaranteed to be random)!
     *
     * @throws UnsupportedOperationException if group is not cyclic or it's too hard to compute a generator
     */
    GroupElementImpl getGenerator() throws UnsupportedOperationException;

    /**
     * Returns true if this group is known to be commutative.
     */
    boolean isCommutative();

    /**
     * Retrieves number of elements in the group if possible.
     *
     * @return size of this group (number of group elements in it) or null if infinite
     * @throws UnsupportedOperationException if the number of elements is unknown or is too expensive to compute
     */
    BigInteger size() throws UnsupportedOperationException;

    /**
     * Returns true if the size of this structure is known and prime.
     */
    boolean hasPrimeSize();

    /**
     * Indicates whether this group implements its own custom exponentiation algorithm by overwriting {@link #exp}.
     *
     * @return true if the group overwrites {@link #exp}, else false
     */
    default boolean implementsOwnExp() {
        return false;
    }

    /**
     * Can be overwritten to implement a custom exponentiation algorithm for the group.
     *
     * @param base the base of the exponentiation
     * @param exponent the exponent of the exponentiation
     * @param precomputation a set of precomputations that can be used to speed up the exponentiation
     * @return the result of {@code base} base to the power of {@code exponent}
     */
    default GroupElementImpl exp(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation) {
        throw new UnsupportedOperationException("Exponentiation is not implemented for group " + this);
    }

    /**
     * Indicates whether this group implements its own multi-exponentiation algorithm
     * by overwriting {@link #multiexp(Multiexponentiation)}.
     *
     * @return true if the group implements its own multi-exponentiation algorithm, else false
     */
    default boolean implementsOwnMultiExp() {
        return false;
    }

    /**
     * Can be overwritten to implement a custom multi-exponentiation algorithm for the group.
     *
     * @param mexp contains the multi-exponentiation terms
     * @return result of computing the multi-exponentiation
     */
    default GroupElementImpl multiexp(Multiexponentiation mexp) {
        throw new UnsupportedOperationException("Multi-exponentiation is not implemented for group " + this);
    }

    /**
     * Estimates the number of inversions that can be done per group operation for the same cost.
     * For example, {@code 2} would mean that an inversion costs half as much as a group operation, on average.
     * @return Estimated number of inversions that can be done per group operation for the same cost
     */
    double estimateCostInvPerOp();

    @Override
    default GroupElementImpl restoreFromRepresentation(Type type, Representation repr) {
        if (!(type instanceof Class && GroupElementImpl.class.isAssignableFrom((Class) type)))
            throw new IllegalArgumentException("Group cannot recreate type "+type.getTypeName()+" from representation");

        return restoreElement(repr);
    }

    /**
     * Returns the number of bytes returned by this structure's {@link Element#getUniqueByteRepresentation()},
     * or an empty {@code Optional} if this structure's elements do not guarantee a fixed length.
     * <p>
     * For example, elements of {@link Zp} will always be represented by {@code ceil(ceil(log(p))/8)} bytes,
     * hence {@code getUniqueByteLength()} would return {@code ceil(ceil(log(p))/8)}.
     * <p>
     * A polynomial ring would return an empty {@code Optional} since a polynomial's unique byte representation length
     * depends on its degree.
     *
     * @return the guaranteed fixed length of {@code getUniqueByteRepresentation()},
     *         or an empty {@code Optional}, if no guarantee
     */
    Optional<Integer> getUniqueByteLength();
}

