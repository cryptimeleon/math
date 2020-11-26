package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link ExponentExpr} representing a constant integer value.
 */
public class ExponentConstantExpr implements ExponentExpr {
    private final BigInteger exponent;

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
    public ExponentExpr substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return this;
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }
}
