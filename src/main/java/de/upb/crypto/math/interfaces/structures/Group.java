package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.RepresentationRestorer;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
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
     * Computes the value of the PowProductExpressions
     * This will usually be more efficient than
     * naively computing that product.
     *
     * @param expr a PowProductExpression
     * @throws IllegalArgumentException if an element is of the wrong type (e.g., incompatible group elements)
     */
    default GroupElement evaluate(PowProductExpression expr) throws IllegalArgumentException {
        expr = expr.dynamicOptimization();

        //Simultaneous exponentiations. Assumes that exponents are nonnegative (ensured by preceeding PowProductExpression.forEach contract)
        int largestExponentBitLength = expr.getLargestExponentBitLength();
        GroupElement result = getNeutralElement();
        Map<GroupElement, BigInteger> factors = expr.getExpression();
        for (int i = largestExponentBitLength; i >= 0; i--) {
            result = result.op(result); //shared among all group elements of this product
            for (Map.Entry<? extends GroupElement, BigInteger> entry : factors.entrySet()) {
                if (entry.getValue().testBit(i)) {
                    result = result.op(entry.getKey());
                }
            }
        }
        return result;
    }

    /**
     * Computes the value of the PowProductExpressions
     * This will usually be more efficient than
     * naively computing that product.
     * <p>
     * The result is being processed on another thread.
     * The returned value is a FutureGroupElement. The actual result
     * can be retrieved by calling get() on the FutureGroupElement.
     * This blocks the thread until the value has been computed.
     * <p>
     * The prefered way to utilize this method is to call this method
     * a bunch of times, then do something with the results, e.g.,
     * FutureGroupElement lhs = evaluateConcurrent(exprLhs);
     * FutureGroupElement rhs = evaluateConcurrent(exprRhs); //will be evaluated at the same time as lhs.
     * bilinearMap.apply(lhs.get(), rhs.get()); //will block until both lhs and rhs are evaluated.
     *
     * @param expr a PowProductExpression
     * @throws IllegalArgumentException if an element is of the wrong type (e.g., incompatible group elements)
     */
    default FutureGroupElement evaluateConcurrent(PowProductExpression expr) throws IllegalArgumentException {
        return new FutureGroupElement(() -> evaluate(expr));
    }

    /**
     * Computes the product of e[i]^exponents[i].
     * This will usually be more efficient than
     * naively computing that product.
     *
     * @param elements  group elements
     * @param exponents exponents for the group elements
     * @return the product of e[i]^exponents[i]
     * @throws IllegalArgumentException if an element is of the wrong type or the list lengths are mismatched
     */
    default GroupElement evaluate(List<? extends GroupElement> elements, List<BigInteger> exponents) throws IllegalArgumentException {
        if (elements.size() != exponents.size())
            throw new IllegalArgumentException("Mismatch between number of operands (" + elements.size() + ") and number of exponents (" + exponents.size() + ")");

        if (!isCommutative()) {
            GroupElement result = getNeutralElement();
            for (int i = 0; i < elements.size(); i++)
                result = result.op(elements.get(i).pow(exponents.get(i)));
            return result;
        }

        PowProductExpression expr = new PowProductExpression(this);
        for (int i = 0; i < elements.size(); i++)
            expr.op(elements.get(i), exponents.get(i));
        return evaluate(expr);
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
     * Returns a PowProductExpression containing this group element.
     */
    default PowProductExpression powProductExpression() {
        return new PowProductExpression(this);
    }

    @Override
    default GroupElement recreateFromRepresentation(Type type, Representation repr) {
        if (!(type instanceof Class && GroupElement.class.isAssignableFrom((Class) type)))
            throw new IllegalArgumentException("Group cannot recreate type "+type.getTypeName()+" from representation");

        return getElement(repr);
    }
}
