package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link ExponentExpr} representing a constant integer value.
 */
public class ExponentConstantExpr implements ExponentExpr {
    /**
     * The constant value of this {@code ExponentConstantExpr}.
     */
    protected final BigInteger exponent;

    public ExponentConstantExpr(BigInteger exponent) {
        this.exponent = exponent;
    }

    public ExponentConstantExpr(Zn.ZnElement exponent) {
        this.exponent = exponent.getInteger();
    }

    public ExponentConstantExpr(long exponent) {
        this.exponent = BigInteger.valueOf(exponent);
    }

    @Override
    public BigInteger evaluate() {
        return exponent;
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return zn.valueOf(exponent);
    }

    @Override
    public ExponentExpr substitute(Substitution substitutions) {
        return this;
    }

    @Override
    public ExponentSumExpr linearize() throws IllegalArgumentException {
        return new ExponentSumExpr(this, new ExponentEmptyExpr());
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }
}
