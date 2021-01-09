package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;

import java.util.function.Consumer;

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
}
