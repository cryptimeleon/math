package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;

public interface BooleanExpression extends Expression {
    @Override
    BooleanExpression substitute(Substitution substitutions);

    @Override
    default BooleanExpression substitute(String variable, Expression substitution) {
        return (BooleanExpression) Expression.super.substitute(variable, substitution);
    }

    @Override
    default BooleanExpression substitute(VariableExpression variable, Expression substitution) {
        return (BooleanExpression) Expression.super.substitute(variable, substitution);
    }

    @Override
    default Boolean evaluate() {
        return evaluate(e -> null);
    }

    @Override
    Boolean evaluate(Substitution substitutions);

    default BooleanExpression and(BooleanExpression rhs) {
        return new BoolAndExpr(this, rhs);
    }

    default BooleanExpression or(BooleanExpression rhs) {
        return new BoolOrExpr(this, rhs);
    }

    default BooleanExpression not() {
        return new BoolNotExpr(this);
    }
}
