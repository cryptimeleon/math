package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link ExponentExpr} representing an empty value useful for instantiating a new expression.
 * <p>
 * Combination with other exponent expressions leads to this expression being replaced by an evaluatable expression.
 * Cannot be evaluated on its own.
 */
public class ExponentEmptyExpr implements ExponentExpr {
    @Override
    public BigInteger evaluate() {
        return BigInteger.ZERO;
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return zn.getZeroElement();
    }

    @Override
    public ExponentExpr substitute(Substitution substitutions) {
        return this;
    }

    @Override
    public ExponentExpr invert() {
        throw new ArithmeticException("division by 0");
    }

    @Override
    public ExponentExpr mul(ExponentExpr other) {
        return this;
    }

    @Override
    public ExponentSumExpr linearize() throws IllegalArgumentException {
        return new ExponentSumExpr(this, this);
    }

    @Override
    public ExponentExpr add(ExponentExpr other) {
        return other;
    }

    @Override
    public ExponentExpr sub(ExponentExpr other) {
        return other.negate();
    }
}
