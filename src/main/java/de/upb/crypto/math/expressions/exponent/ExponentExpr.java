package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.bool.ExponentEqualityExpr;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Function;

public interface ExponentExpr extends Expression {
    @Override
    BigInteger evaluate();
    Zn.ZnElement evaluate(Zn zn);

    @Override
    default BigInteger evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        return substitute(substitutions).evaluate();
    }

    default Zn.ZnElement evaluate(Zn zn, Function<VariableExpression, ? extends Expression> substitutions) {
        return substitute(substitutions).evaluate(zn);
    }

    @Override
    default ExponentExpr substitute(String variable, Expression substitution) {
        return (ExponentExpr) Expression.super.substitute(variable, substitution);
    }

    @Override
    ExponentExpr substitute(Function<VariableExpression, ? extends Expression> substitutions);

    @Override
    default ExponentExpr substitute(ValueBundle values) {
        return (ExponentExpr) Expression.super.substitute(values);
    }

    default ExponentExpr negate() {
        return new ExponentNegExpr(this);
    }

    default ExponentExpr invert() {
        return new ExponentInvExpr(this);
    }

    default ExponentExpr add(ExponentExpr other) {
        return new ExponentSumExpr(this, other);
    }

    default ExponentExpr add(String other) {
        return add(new ExponentVariableExpr(other));
    }

    default ExponentExpr sub(ExponentExpr other) {
        return add(other.negate());
    }

    default ExponentExpr sub(String other) {
        return sub(new ExponentVariableExpr(other));
    }

    default ExponentExpr mul(ExponentExpr other) {
        return new ExponentMulExpr(this, other);
    }

    default ExponentExpr mul(BigInteger other) {
        return mul(new ExponentConstantExpr(other));
    }

    default ExponentExpr mul(long other) {
        return mul(BigInteger.valueOf(other));
    }

    default ExponentExpr mul(String other) {
        return mul(new ExponentVariableExpr(other));
    }

    default ExponentExpr pow(ExponentExpr exponent) {
        return new ExponentPowExpr(this, exponent);
    }

    default ExponentExpr pow(long exponent) {
        return pow(BigInteger.valueOf(exponent));
    }

    default ExponentExpr pow(BigInteger exponent) {
        return pow(new ExponentConstantExpr(exponent));
    }

    default ExponentExpr pow(String exponent) {
        return pow(new ExponentVariableExpr(exponent));
    }

    default ExponentEqualityExpr isEqualTo(ExponentExpr other) {
        return new ExponentEqualityExpr(this, other);
    }

    default ExponentEqualityExpr isEqualTo(Zn.ZnElement other) {
        return new ExponentEqualityExpr(this, other.asExponentExpression());
    }

    default ExponentEqualityExpr isEqualTo(BigInteger other) {
        return new ExponentEqualityExpr(this, new ExponentConstantExpr(other));
    }
}
