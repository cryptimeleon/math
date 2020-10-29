package de.upb.crypto.math.interfaces.structures.group.impl;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;
import de.upb.crypto.math.structures.groups.exp.SmallExponentPrecomputation;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Optional;

/**
 * A Group. Operations are defined on its elements.
 */
public interface GroupImpl extends StandaloneRepresentable, RepresentationRestorer {
    /**
     * Returns the neutral element for this group
     */
    GroupElementImpl getNeutralElement();

    GroupElementImpl getUniformlyRandomElement() throws UnsupportedOperationException;

    default GroupElementImpl getUniformlyRandomNonNeutral() throws UnsupportedOperationException {
        GroupElementImpl result;
        do {
            result = getUniformlyRandomElement();
        } while (result.isNeutralElement());

        return result;
    }

    GroupElementImpl getElement(Representation repr);

    /**
     * Returns any generator of this group if the group is cyclic and it's feasible to compute a generator.
     * Repeated calls may or may not always supply the same generator again (i.e. the output is not guaranteed to be random)!
     *
     * @throws UnsupportedOperationException if group is not cyclic or it's hard to compute a generator
     */
    GroupElementImpl getGenerator() throws UnsupportedOperationException;

    /**
     * Returns true if this group is known to be commutative.
     */
    boolean isCommutative();

    /**
     * Size of the group
     *
     * @return size of this group (number of group elements in it) or null if infinite
     * @throws UnsupportedOperationException if the number of elements is unknown / too expensive to compute
     */
    BigInteger size() throws UnsupportedOperationException;

    /**
     * Returns true if the size of this structure is known and prime.
     */
    boolean hasPrimeSize();

    default boolean implementsOwnExp() {
        return false;
    }

    default GroupElementImpl exp(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation) {
        throw new UnsupportedOperationException("Exponentiation is not implemented for group " + this);
    }

    /**
     * Indicates whether this group implements its own multi-exponentiation algorithm.
     * @return {@code true} if the group implements its own multi-exponentiation algorithm {@code false} otherwise
     */
    default boolean implementsOwnMultiExp() {
        return false;
    }

    /**
     * Allows the group to implement its own multi-exponentiation algorithm.
     * @param mexp  Contains the multi-exponentiation terms
     * @return Result of computing the multi-exponentiation
     */
    default GroupElementImpl multiexp(Multiexponentiation mexp) {
        throw new UnsupportedOperationException("Multi-exponentiation is not implemented for group " + this);
    }

    /**
     * Estimates the number of inversions that can be done per group operation for the same cost.
     * For example, {@code 2} would mean that an inversion costs half as much as a group operation, on average.
     * @return Estimated number of inversions that can be done per group operation for the same cost
     */
    default double estimateCostInvPerOp() {
        return 1;
    }

    @Override
    default GroupElementImpl recreateFromRepresentation(Type type, Representation repr) {
        if (!(type instanceof Class && GroupElementImpl.class.isAssignableFrom((Class) type)))
            throw new IllegalArgumentException("Group cannot recreate type "+type.getTypeName()+" from representation");

        return getElement(repr);
    }

    Optional<Integer> getUniqueByteLength();
}

