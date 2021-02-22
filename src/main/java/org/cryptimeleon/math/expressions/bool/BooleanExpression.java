package org.cryptimeleon.math.expressions.bool;

import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.expressions.VariableExpression;

/**
 * An {@link Expression} that evaluates to a {@code Boolean}.
 */
public interface BooleanExpression extends Expression {
    BoolConstantExpr TRUE = new BoolConstantExpr(true);
    BoolConstantExpr FALSE = new BoolConstantExpr(false);

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
     * Evaluates the result of this expression (with the given substitutions) concurrently in the background. <br>
     * The result can be retrieved by calling getResult() on the return value.
     */
    LazyBoolEvaluationResult evaluateLazy(Substitution substitutions);

    /**
     * Evaluates the result of this expression concurrently in the background. Result can be retrieved
     * by calling getResult() on the return value.<br>
     * Use this for (potentially) more computationally expensive expressions.
     */
    default LazyBoolEvaluationResult evaluateLazy() {
        return evaluateLazy(e -> null);
    }


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

    static BooleanExpression valueOf(boolean bool) {
        return bool ? TRUE : FALSE;
    }
}
