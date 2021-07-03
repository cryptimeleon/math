package org.cryptimeleon.math.structures.cartesian;

import org.cryptimeleon.math.expressions.exponent.ExponentExpr;
import org.cryptimeleon.math.expressions.group.GroupElementExpression;
import org.cryptimeleon.math.expressions.group.GroupEmptyExpr;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearMap;
import org.cryptimeleon.math.structures.rings.RingElement;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A vector of expressions supporting various operations on its elements.
 */
public class GroupElementExpressionVector extends Vector<GroupElementExpression> {

    public GroupElementExpressionVector(GroupElementExpression... values) {
        super(values);
    }

    public GroupElementExpressionVector(List<GroupElementExpression> values) {
        super(values);
    }

    public GroupElementExpressionVector(Vector<? extends GroupElementExpression> vector) {
        this(vector.values, true);
    }

    protected GroupElementExpressionVector(GroupElementExpression[] values, boolean isSafe) {
        super(values, isSafe);
    }

    protected GroupElementExpressionVector(List<? extends GroupElementExpression> values, boolean isSafe) {
        super(values, isSafe);
    }

    public GroupElementExpressionVector op(Vector<? extends GroupElementExpression> other) {
        return zip(other, GroupElementExpression::op, GroupElementExpressionVector::instantiateWithSafeArray);
    }

    public GroupElementExpressionVector op(GroupElement elem) {
        return map(g -> g.op(elem), GroupElementExpressionVector::instantiateWithSafeArray);
    }

    public GroupElementExpressionVector pow(BigInteger exponent) {
        return map(g -> g.pow(exponent), GroupElementExpressionVector::instantiateWithSafeArray);
    }

    public GroupElementExpressionVector pow(long exponent) {
        return map(g -> g.pow(exponent), GroupElementExpressionVector::instantiateWithSafeArray);
    }

    public GroupElementExpressionVector pow (RingElement exponent) {
        return map(g -> g.pow(exponent), GroupElementExpressionVector::instantiateWithSafeArray);
    }

    public GroupElementExpressionVector pow(ExponentExpr exponent) {
        return map(g -> g.pow(exponent), GroupElementExpressionVector::instantiateWithSafeArray);
    }

    public GroupElementExpressionVector pow(Vector<?> exponents) {
        return zip(exponents, GroupElementExpressionVector::exponentiateWithObject, GroupElementExpressionVector::instantiateWithSafeArray);
    }

    public GroupElementExpression innerProduct(Vector<?> other, GroupElementExpression neutralElement) {
        return zipReduce(other, GroupElementExpressionVector::exponentiateWithObject, GroupElementExpression::op, neutralElement);
    }

    public GroupElementExpression innerProduct(Vector<?> other) {
        return zipReduce(other, GroupElementExpressionVector::exponentiateWithObject, GroupElementExpression::op, new GroupEmptyExpr());
    }

    protected static GroupElementExpression exponentiateWithObject(GroupElementExpression g, Object exp) {
        if (exp instanceof BigInteger)
            return g.pow((BigInteger) exp);
        if (exp instanceof RingElement)
            return g.pow((RingElement) exp);
        if (exp instanceof Long)
            return g.pow((Long) exp);
        if (exp instanceof ExponentExpr)
            return g.pow((ExponentExpr) exp);
        if (exp instanceof String)
            return g.pow((String) exp);
        throw new IllegalArgumentException("Cannot compute g^"+exp.getClass().getName());
    }

    public GroupElementExpression innerProduct(Vector<? extends GroupElementExpression> rightHandSide, BilinearMap bilinearMap) {
        return zipReduce(rightHandSide, bilinearMap::applyExpr, GroupElementExpression::op, bilinearMap.getGT().expr());
    }

    static GroupElementExpressionVector instantiateWithSafeArray(List<? extends GroupElementExpression> array) {
        return new GroupElementExpressionVector(array, true);
    }

    public static GroupElementExpressionVector iterate(GroupElementExpression initialValue, Function<GroupElementExpression, GroupElementExpression> nextValue, int n) {
        return Vector.iterate(initialValue, nextValue, n, GroupElementExpressionVector::instantiateWithSafeArray);
    }

    public static GroupElementExpressionVector generate(Function<Integer, ? extends GroupElementExpression> generator, int n) {
        return generatePlain(generator, n, GroupElementExpressionVector::instantiateWithSafeArray);
    }

    public static GroupElementExpressionVector generate(Supplier<? extends GroupElementExpression> generator, int n) {
        return generatePlain(generator, n, GroupElementExpressionVector::instantiateWithSafeArray);
    }

    public static GroupElementExpressionVector of(GroupElementExpression... vals) {
        return new GroupElementExpressionVector(vals, false);
    }

    @Override
    public GroupElementExpressionVector pad(GroupElementExpression valueToPadWith, int desiredLength) {
        return new GroupElementExpressionVector(super.pad(valueToPadWith, desiredLength));
    }

    @Override
    public GroupElementExpressionVector append(GroupElementExpression valueToAppend) {
        return new GroupElementExpressionVector(super.append(valueToAppend));
    }

    @Override
    public GroupElementExpressionVector prepend(GroupElementExpression valueToPrepend) {
        return new GroupElementExpressionVector(super.prepend(valueToPrepend));
    }

    @Override
    public GroupElementExpressionVector replace(int index, GroupElementExpression substitute) {
        return new GroupElementExpressionVector(super.replace(index, substitute));
    }

    @Override
    public GroupElementExpressionVector truncate(int newLength) {
        return new GroupElementExpressionVector(super.truncate(newLength));
    }

    public GroupElementExpressionVector concatenate(Vector<? extends GroupElementExpression> secondPart) {
        return new GroupElementExpressionVector(super.concatenate(secondPart));
    }

    public static GroupElementExpressionVector fromStream(Stream<? extends GroupElementExpression> stream) {
        return fromStreamPlain(stream, GroupElementExpressionVector::instantiateWithSafeArray);
    }
}
