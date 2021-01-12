package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;

/**
 * An {@link Expression} that evaluates to a {@code Boolean}.
 */
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

    /**
     * Applies a Boolean AND to this and the given Boolean expression.
     * @return a new Boolean expression representing the Boolean AND of this and the given Boolean expression
     */
    default BooleanExpression and(BooleanExpression rhs) {
        return new BoolAndExpr(this, rhs);
    }

    /**
     * Applies a Boolean OR to this and the given Boolean expression.
     * @return a new Boolean expression representing the Boolean OR of this and the given Boolean expression
     */
    default BooleanExpression or(BooleanExpression rhs) {
        return new BoolOrExpr(this, rhs);
    }

    /**
     * Applies a Boolean NOT to this Boolean expression.
     * @return a new Boolean expression representing the Boolean NOT of this Boolean expression.
     */
    default BooleanExpression not() {
        return new BoolNotExpr(this);
    }
}
