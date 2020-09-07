package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupEmptyExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;
import de.upb.crypto.math.structures.zn.Zn;

import java.lang.reflect.Type;
import java.math.BigInteger;

/**
 * A Group. Operations are defined on its elements.
 */
public interface Group extends Structure, RepresentationRestorer {
    /**
     * Returns the neutral element for this group
     */
    GroupElement getNeutralElement();

    @Override
    GroupElement getUniformlyRandomElement() throws UnsupportedOperationException;

    default GroupElement getUniformlyRandomNonNeutral() {
        GroupElement result;
        do {
            result = getUniformlyRandomElement();
        } while (result.isNeutralElement());
        return result;
    }

    @Override
    GroupElement getElement(Representation repr);

    /**
     * Returns any generator of this group if the group is cyclic and it's feasible to compute a generator.
     * Repeated calls may or may not always supply the same generator again (i.e. the output is not guaranteed to be random)!
     *
     * @throws UnsupportedOperationException
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
    default GroupElement recreateFromRepresentation(Type type, Representation repr) {
        if (!(type instanceof Class && GroupElement.class.isAssignableFrom((Class) type)))
            throw new IllegalArgumentException("Group cannot recreate type "+type.getTypeName()+" from representation");

        return getElement(repr);
    }

    /**
     * Returns Zn, where n = size()
     */
    default Zn getZn() {
        BigInteger size = size();
        if (size == null)
            throw new IllegalArgumentException("Infinitely large group - cannot output corresponding Zn");

        return new Zn(size);
    }

    /**
     * Returns a random integer between 0 and size()-1.
     */
    default Zn.ZnElement getUniformlyRandomExponent() {
        return getZn().getUniformlyRandomElement();
    }

    /**
     * Returns a random integer invertible mod size().
     */
    default Zn.ZnElement getUniformlyRandomUnitExponent() {
        return getZn().getUniformlyRandomUnit();
    }

    /**
     * Returns a random integer between 1 and size()-1.
     */
    default Zn.ZnElement getUniformlyRandomNonzeroExponent() {
        return getZn().getUniformlyRandomNonzeroElement();
    }
}

