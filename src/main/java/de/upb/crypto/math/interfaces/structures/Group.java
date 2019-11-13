package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.expressions.group.*;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;
import de.upb.crypto.math.structures.zn.Zn;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Group. Operations are defined on its elements.
 */
public interface Group extends Structure, RepresentationRestorer {
    /**
     * Thread pool used for concurrent evaluation of multiple PowProductExpressions
     */
    public static ExecutorService executor = Executors.newWorkStealingPool();

    /**
     * Returns the neutral element for this group
     */
    GroupElement getNeutralElement();

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException;

    public default GroupElement getUniformlyRandomNonNeutral() throws UnsupportedOperationException {
        GroupElement result;
        do {
            result = getUniformlyRandomElement();
        } while (result.isNeutralElement());

        return result;
    }

    @Override
    GroupElement getElement(Representation repr);

    /**
     * Returns any generator of this group if the group is cyclic and it's feasible to compute a generator. Repeated calls may or may not always supply the same generator again (i.e. the output is not guaranteed to be random)!
     *
     * @throws UnsupportedOperationException
     */
    default GroupElement getGenerator() throws UnsupportedOperationException {
        if (size().isProbablePrime(10000))
            return getUniformlyRandomNonNeutral();
        throw new UnsupportedOperationException("Can't compute generator for group: " + this);
    }

    /**
     * Returns true if this group is known to be commutative.
     */
    boolean isCommutative();

    /**
     * Outputs an integer x such that 100 inversion operations
     * cost roughly as much computation time as x group operations.
     */
    int estimateCostOfInvert();

    /**
     * Returns a GroupElementExpression containing the neutral group element.
     */
    default GroupElementExpression expr() {
        return new GroupElementConstantExpr(this.getNeutralElement());
    }

    @Override
    default GroupElement recreateFromRepresentation(Type type, Representation repr) {
        if (!(type instanceof Class && GroupElement.class.isAssignableFrom((Class) type)))
            throw new IllegalArgumentException("Group cannot recreate type "+type.getTypeName()+" from representation");

        return getElement(repr);
    }
    /**
     * Returns the suggested evaluator for GroupElementExpressions for elements of this group.
     * If this group is part of a larger structure (e.g., a BilinearGroup), the evaluator ideally should optimize for that complete super structure.
     *
     * @return an evaluator that is able to handle all expressions that legitimately involve an element of this group (this includes PairingExpr).
     */
    default GroupElementExpressionEvaluator getExpressionEvaluator() {
        return new NaiveGroupElementExpressionEvaluator();
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
}

