package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link ExponentExpr} representing an exponentiation with both base and exponent being exponent expressions.
 */
public class ExponentPowExpr implements ExponentExpr {
    private final ExponentExpr base, exponent;

    public ExponentPowExpr(ExponentExpr base, ExponentExpr exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    /**
     * Retrieves the base exponent expression of this exponentiation.
     */
    public ExponentExpr getBase() {
        return base;
    }

    /**
     * Retrieves the power exponent expression of this exponentiation.
     */
    public ExponentExpr getExponent() {
        return exponent;
    }

    @Override
    public BigInteger evaluate() {
        return base.evaluate().pow(exponent.evaluate().intValueExact());
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(base);
        action.accept(exponent);
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return base.evaluate(zn).pow(exponent.evaluate());
    }

    @Override
    public ExponentExpr substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return base.substitute(substitutions).pow(exponent.substitute(substitutions));
    }

}
