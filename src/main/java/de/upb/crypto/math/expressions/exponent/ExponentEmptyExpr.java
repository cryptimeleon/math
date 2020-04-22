package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExponentEmptyExpr implements ExponentExpr {
    @Override
    public BigInteger evaluate() {
        return BigInteger.ZERO;
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return zn.getZeroElement();
    }

    @Override
    public ExponentEmptyExpr substitute(Substitutions variableValues) {
        return this;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        //Intentionally empty.
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
    public ExponentExpr add(ExponentExpr other) {
        return other;
    }

    @Override
    public ExponentExpr sub(ExponentExpr other) {
        return other.negate();
    }

    @Override
    public ExponentExpr precompute() {
        return this;
    }
}
