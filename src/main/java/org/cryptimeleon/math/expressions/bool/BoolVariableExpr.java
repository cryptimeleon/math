package org.cryptimeleon.math.expressions.bool;

import org.cryptimeleon.math.expressions.EvaluationException;
import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.expressions.VariableExpression;

import java.util.function.Consumer;
/**
 * A {@link BooleanExpression} representing a variable, a Boolean whose actual Boolean value is not currently known.
 */
public interface BoolVariableExpr extends VariableExpression, BooleanExpression {
    @Override
    default Boolean evaluate() {
        throw new EvaluationException(this, "Variable has no value");
    }

    @Override
    default Boolean evaluate(Substitution substitutions) {
        BooleanExpression substitution = (BooleanExpression) substitutions.getSubstitution(this);
        if (substitution == null)
            throw new EvaluationException(this, "Variable cannot be evaluated");
        return substitution.evaluate();
    }

    @Override
    default void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }

    @Override
    default BooleanExpression substitute(Substitution substitutions) {
        Expression replacement = substitutions.getSubstitution(this);
        if (replacement != null)
            return (BooleanExpression) replacement;
        return this;
    }

    @Override
    default LazyBoolEvaluationResult evaluateLazy(Substitution substitutions) {
        BooleanExpression substitution = (BooleanExpression) substitutions.getSubstitution(this);
        if (substitution == null)
            throw new EvaluationException(this, "Variable cannot be evaluated");
        return substitution.evaluateLazy();
    }
}
