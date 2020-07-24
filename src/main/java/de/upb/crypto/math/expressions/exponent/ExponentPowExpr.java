package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExponentPowExpr implements ExponentExpr {
    protected ExponentExpr base, exponent;

    public ExponentPowExpr(ExponentExpr base, ExponentExpr exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    public ExponentExpr getBase() {
        return base;
    }

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
