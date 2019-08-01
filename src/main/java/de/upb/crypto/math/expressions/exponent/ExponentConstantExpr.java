package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExponentConstantExpr implements ExponentExpr {
    protected BigInteger exponent;

    public ExponentConstantExpr(BigInteger exponent) {
        this.exponent = exponent;
    }

    public ExponentConstantExpr(Zn.ZnElement exponent) {
        this.exponent = exponent.getInteger();
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
    public ExponentConstantExpr substitute(Function<String, Expression> substitutionMap) {
        return this;
    }

    @Override
    public ExponentExpr precompute() {
        return this;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
    }
}
