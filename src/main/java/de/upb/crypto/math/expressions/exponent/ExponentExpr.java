package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.bool.ExponentEqualityExpr;
import de.upb.crypto.math.expressions.group.GroupOpExpr;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

public interface ExponentExpr extends Expression {
    @Override
    BigInteger evaluate();
    Zn.ZnElement evaluate(Zn zn);

    @Override
    default BigInteger evaluate(Substitution substitutions) {
        return substitute(substitutions).evaluate();
    }

    default Zn.ZnElement evaluate(Zn zn, Substitution substitutions) {
        return substitute(substitutions).evaluate(zn);
    }

    @Override
    default ExponentExpr substitute(String variable, Expression substitution) {
        return (ExponentExpr) Expression.super.substitute(variable, substitution);
    }

    @Override
    default ExponentExpr substitute(VariableExpression variable, Expression substitution) {
        return (ExponentExpr) Expression.super.substitute(variable, substitution);
    }

    @Override
    ExponentExpr substitute(Substitution substitutions);

    default ExponentExpr negate() {
        return new ExponentNegExpr(this);
    }

    default ExponentExpr invert() {
        return new ExponentInvExpr(this);
    }

    default ExponentExpr add(ExponentExpr other) {
        return new ExponentSumExpr(this, other);
    }

    default ExponentExpr add(Zn.ZnElement other) {
        return add(other.asExponentExpression());
    }

    default ExponentExpr add(String other) {
        return add(new BasicNamedExponentVariableExpr(other));
    }

    default ExponentExpr sub(ExponentExpr other) {
        return add(other.negate());
    }

    default ExponentExpr sub(Zn.ZnElement other) {
        return sub(other.asExponentExpression());
    }

    default ExponentExpr sub(String other) {
        return sub(new BasicNamedExponentVariableExpr(other));
    }

    default ExponentExpr mul(ExponentExpr other) {
        return new ExponentMulExpr(this, other);
    }

    default ExponentExpr mul(BigInteger other) {
        return mul(new ExponentConstantExpr(other));
    }

    default ExponentExpr mul(RingElement other) { return mul(other.asInteger()); }

    default ExponentExpr mul(long other) {
        return mul(BigInteger.valueOf(other));
    }

    default ExponentExpr mul(String other) {
        return mul(new BasicNamedExponentVariableExpr(other));
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

    default ExponentExpr pow(RingElement exponent) {
        return pow(exponent.asInteger());
    }

    default ExponentExpr pow(String exponent) {
        return pow(new BasicNamedExponentVariableExpr(exponent));
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


    /**
     * Returns an equivalent expression of the form y + f(variables), where y is constant (no variables), and the expression f is linear, which means that
     * f(variables) + f(variables2) = f(variables + variables2)
     *
     * The exact result is a ExponentSumExpr
     * where the left-hand-side y has !y.containsVariables(),
     * the right-hand-side is linear
     *
     * @throws IllegalArgumentException if it's not possible to form the desired output (e.g., the input is something like g^(x_1 * x_2) for variables x_1, x_2).
     */
    ExponentSumExpr linearize() throws IllegalArgumentException;
}
