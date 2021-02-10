package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;

import java.util.function.Consumer;

/**
 * A {@link BooleanExpression} representing an empty expression useful for instantiating a new Boolean expression.
 * <p>
 * This class is useful when first creating a new Boolean expression, i.e. as en empty scaffolding.
 * It will "disappear" once combined (via AND or OR) with other Boolean expressions.
 * <p>
 * Cannot be evaluated.
 */
public class BoolEmptyExpr implements BooleanExpression {

    @Override
    public BooleanExpression substitute(Substitution substitutions) {
        return this;
    }

    @Override
    public Boolean evaluate() {
        throw new EvaluationException(this, "Cannot evaluate an empty expression.");
    }

    @Override
    public Boolean evaluate(Substitution substitutions) {
        return evaluate();
    }

    @Override
    public LazyBoolEvaluationResult evaluateLazy(Substitution substitutions) {
        throw new EvaluationException(this, "Cannot evaluate an empty expression.");
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }

    @Override
    public BooleanExpression or(BooleanExpression rhs) {
        return rhs;
    }

    @Override
    public BooleanExpression and(BooleanExpression rhs) {
        return rhs;
    }

    @Override
    public BooleanExpression not() {
        return this;
    }
}
