package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;

import java.util.function.Consumer;
import java.util.function.Function;

public class BoolVariableExpr implements VariableExpression, BooleanExpression {
    protected final String name;

    public BoolVariableExpr(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BooleanExpression substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        Expression replacement = substitutions.apply(this);
        if (replacement != null)
            return (BooleanExpression) replacement;
        return this;
    }

    @Override
    public Boolean evaluate() {
        throw new EvaluationException(this, "Variable has no value");
    }

    @Override
    public Boolean evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        BooleanExpression substitution = (BooleanExpression) substitutions.apply(this);
        if (substitution == null)
            throw new EvaluationException(this, "Variable cannot be evaluated");
        return substitution.evaluate();
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }

}
