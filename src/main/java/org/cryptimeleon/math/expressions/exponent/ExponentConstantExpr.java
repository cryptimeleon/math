package org.cryptimeleon.math.expressions.exponent;

import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;

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
        this.exponent = exponent.asInteger();
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
