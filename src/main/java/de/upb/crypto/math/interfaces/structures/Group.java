package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupEmptyExpr;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;
import de.upb.crypto.math.structures.cartesian.GroupElementVector;
import de.upb.crypto.math.structures.cartesian.RingElementVector;
import de.upb.crypto.math.structures.zn.Zn;

import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * An algebraic group.
 * <p>
 * Usually used as a wrapper around a {@link GroupImpl} to offer additional evaluation capabilities.
 * You should use {@link GroupImpl} for your implementation instead.
 */
public interface Group extends Structure, RepresentationRestorer {
    /**
     * Returns the neutral element for this group
     */
    GroupElement getNeutralElement();

    @Override
    GroupElement getUniformlyRandomElement() throws UnsupportedOperationException;

    @Override
    default GroupElementVector getUniformlyRandomElements(int n) throws UnsupportedOperationException {
        return GroupElementVector.generate(this::getUniformlyRandomElement, n);
    }

    /**
     * Generates a uniformly random non-neutral element of this group using a cryptographically strong RNG.
     */
    default GroupElement getUniformlyRandomNonNeutral() {
        GroupElement result;
        do {
            result = getUniformlyRandomElement();
        } while (result.isNeutralElement());
        return result;
    }

    /**
     * Generates {@code n} uniformly random non-neutral element of this group using a cryptographically strong RNG.
     */
    default GroupElementVector getUniformlyRandomNonNeutrals(int n) {
        return GroupElementVector.generate(this::getUniformlyRandomNonNeutral, n);
    }

    @Override
    GroupElement getElement(Representation repr);

    /**
     * Recreates a {@link GroupElementVector} containing group elements from this {@code Group} from a
     * {@code Representation} of that vector.
     *
     * @param repr a representation of a {@code GroupElementVector}
     *             (obtained via {@link GroupElementVector#getRepresentation()}).
     */
    default GroupElementVector getVector(Representation repr) {
        return GroupElementVector.fromStream(repr.list().stream().map(this::getElement));
    }

    /**
     * Returns any generator of this group if the group is cyclic and it's feasible to compute a generator.
     * <p>
     * Repeated calls may or may not always supply the same generator again
     * (i.e. the output is not guaranteed to be random)!
     *
     * @throws UnsupportedOperationException if the group doesn't know or have a generator
     */
    default GroupElement getGenerator() throws UnsupportedOperationException {
        if (hasPrimeSize())
            return getUniformlyRandomNonNeutral();
        throw new UnsupportedOperationException("Can't compute generator for group: " + this);
    }

    /**
     * Returns true if this group is known to be commutative.
     */
    boolean isCommutative();

    /**
     * Returns a GroupElementExpression containing the neutral group element.
     */
    default GroupElementExpression expr() {
        return new GroupEmptyExpr(this);
    }

    @Override
    default Object recreateFromRepresentation(Type type, Representation repr) {
        if (type instanceof Class && GroupElement.class.isAssignableFrom((Class) type))
            return getElement(repr);
        if (type instanceof Class && GroupElementVector.class.isAssignableFrom((Class) type))
            return getVector(repr);

        throw new IllegalArgumentException("Group cannot recreate type "+type.getTypeName()+" from representation");
    }

    /**
     * Returns {@code Zn}, where {@code n == size()}.
     */
    default Zn getZn() {
        BigInteger size = size();
        if (size == null)
            throw new IllegalArgumentException("Infinitely large group - cannot output corresponding Zn");

        return new Zn(size);
    }

    /**
     * Returns a random integer between {@code 0} and {@code size()-1} (inclusive) using a cryptographically strong RNG.
     */
    default Zn.ZnElement getUniformlyRandomExponent() {
        return getZn().getUniformlyRandomElement();
    }

    /**
     * Returns n random integers between {@code 0} and {@code size()-1} (inclusive) using a cryptographically strong RNG.
     */
    default RingElementVector getUniformlyRandomExponents(int n) {
        return RingElementVector.generate(this::getUniformlyRandomExponent, n);
    }

    /**
     * Returns a random integer invertible mod {@code size()} using a cryptographically strong RNG.
     */
    default Zn.ZnElement getUniformlyRandomUnitExponent() {
        return getZn().getUniformlyRandomUnit();
    }

    /**
     * Returns n random integers invertible mod {@code size()} using a cryptographically strong RNG.
     */
    default RingElementVector getUniformlyRandomUnitExponents(int n) {
        return RingElementVector.generate(this::getUniformlyRandomUnitExponent, n);
    }

    /**
     * Returns a random integer between {@code 1} and {@code size()-1} (inclusive) using a cryptographically strong RNG.
     */
    default Zn.ZnElement getUniformlyRandomNonzeroExponent() {
        return getZn().getUniformlyRandomNonzeroElement();
    }

    /**
     * Returns n random integers between {@code 1} and {@code size()-1} (inclusive) using a cryptographically strong RNG.
     */
    default RingElementVector getUniformlyRandomNonzeroExponents(int n) {
        return RingElementVector.generate(this::getUniformlyRandomNonzeroExponent, n);
    }
}

