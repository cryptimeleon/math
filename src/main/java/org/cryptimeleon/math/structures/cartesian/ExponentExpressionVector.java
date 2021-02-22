package org.cryptimeleon.math.structures.cartesian;

import org.cryptimeleon.math.expressions.exponent.ExponentExpr;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A vector of ring elements supporting element-wise ring operations with other ring element vectors.
 */
public class ExponentExpressionVector extends Vector<ExponentExpr> {

    public ExponentExpressionVector(ExponentExpr... values) {
        super(values);
    }

    public ExponentExpressionVector(List<? extends ExponentExpr> values) {
        super(values);
    }

    protected ExponentExpressionVector(ExponentExpr[] values, boolean isSafe) {
        super(values, isSafe);
    }

    protected ExponentExpressionVector(List<? extends ExponentExpr> values, boolean isSafe) {
        super(values, isSafe);
    }

    public ExponentExpressionVector(Vector<? extends ExponentExpr> vector) {
        this(vector.values, true);
    }

    public ExponentExpressionVector mul(Vector<? extends ExponentExpr> other) {
        return zip(other, ExponentExpr::mul, ExponentExpressionVector::instantiateWithSafeArray);
    }

    public ExponentExpressionVector mul(ExponentExpr elem) {
        return map(g -> g.mul(elem), ExponentExpressionVector::instantiateWithSafeArray);
    }

    public ExponentExpressionVector mul(BigInteger factor) {
        return map(g -> g.mul(factor), ExponentExpressionVector::instantiateWithSafeArray);
    }

    public ExponentExpressionVector mul(Long factor) {
        return map(g -> g.mul(factor), ExponentExpressionVector::instantiateWithSafeArray);
    }

    public ExponentExpressionVector add(ExponentExpr elem) {
        return map(g -> g.add(elem), ExponentExpressionVector::instantiateWithSafeArray);
    }

    public ExponentExpressionVector pow(BigInteger exponent) {
        return map(g -> g.pow(exponent), ExponentExpressionVector::instantiateWithSafeArray);
    }

    public ExponentExpressionVector pow(long exponent) {
        return map(g -> g.pow(exponent), ExponentExpressionVector::instantiateWithSafeArray);
    }

    public ExponentExpressionVector pow(Vector<?> exponents) {
        return zip(exponents, (g,x) ->
                        x instanceof Long ? g.pow((Long) x)
                                : g.pow((BigInteger) x),
                ExponentExpressionVector::new);
    }

    public ExponentExpr innerProduct(Vector<? extends ExponentExpr> rightHandSide, ExponentExpr zeroElement) {
        return zipReduce(rightHandSide, ExponentExpr::mul, ExponentExpr::add, zeroElement);
    }

    public ExponentExpr innerProduct(Vector<? extends ExponentExpr> rightHandSide) {
        return innerProduct(rightHandSide, null);
    }

    private static ExponentExpressionVector instantiateWithSafeArray(List<? extends ExponentExpr> array) {
        return new ExponentExpressionVector(array, true);
    }

    public static ExponentExpressionVector iterate(ExponentExpr initialValue, Function<ExponentExpr, ExponentExpr> nextValue, int n) {
        return Vector.iterate(initialValue, nextValue, n, ExponentExpressionVector::instantiateWithSafeArray);
    }

    public static ExponentExpressionVector generate(Function<Integer, ? extends ExponentExpr> generator, int n) {
        return generatePlain(generator, n, ExponentExpressionVector::instantiateWithSafeArray);
    }

    public static ExponentExpressionVector generate(Supplier<? extends ExponentExpr> generator, int n) {
        return generatePlain(generator, n, ExponentExpressionVector::instantiateWithSafeArray);
    }

    public static ExponentExpressionVector of(ExponentExpr... vals) {
        return new ExponentExpressionVector(vals, false);
    }

    public static ExponentExpressionVector fromStream(Stream<? extends ExponentExpr> stream) {
        return fromStreamPlain(stream, ExponentExpressionVector::instantiateWithSafeArray);
    }

    @Override
    public ExponentExpressionVector pad(ExponentExpr valueToPadWith, int desiredLength) {
        return new ExponentExpressionVector(super.pad(valueToPadWith, desiredLength));
    }

    @Override
    public ExponentExpressionVector replace(int index, ExponentExpr substitute) {
        return new ExponentExpressionVector(super.replace(index, substitute));
    }

    @Override
    public ExponentExpressionVector truncate(int newLength) {
        return new ExponentExpressionVector(super.truncate(newLength));
    }

    public ExponentExpressionVector concatenate(Vector<? extends ExponentExpr> secondPart) {
        return new ExponentExpressionVector(super.concatenate(secondPart));
    }
}
