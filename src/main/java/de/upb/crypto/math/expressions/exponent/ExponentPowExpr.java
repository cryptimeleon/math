package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;

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
    public Zn.ZnElement evaluate(Zn zn) {
        return base.evaluate(zn).pow(exponent.evaluate());
    }

    @Override
    public ExponentPowExpr substitute(Substitutions variableValues) {
        return new ExponentPowExpr(base.substitute(variableValues), exponent.substitute(variableValues));
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        base.treeWalk(visitor);
        exponent.treeWalk(visitor);
    }

    @Override
    public ExponentExpr precompute() {
        return new ExponentPowExpr(base.precompute(), exponent.precompute());
    }
}
