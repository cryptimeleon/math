package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.VariableExpression;

import java.util.function.Function;

public interface BooleanExpression extends Expression {
    @Override
    default BooleanExpression substitute(ValueBundle values) {
        return (BooleanExpression) Expression.super.substitute(values);
    }

    @Override
    BooleanExpression substitute(Function<VariableExpression, ? extends Expression> substitutions);

    @Override
    default BooleanExpression substitute(String variable, Expression substitution) {
        return (BooleanExpression) Expression.super.substitute(variable, substitution);
    }

    @Override
    default Boolean evaluate() {
        return evaluate(e -> null);
    }

    @Override
    Boolean evaluate(Function<VariableExpression, ? extends Expression> substitutions);

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
