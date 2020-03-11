package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.*;

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
    public boolean evaluate() {
        throw new EvaluationException(this, "Variable has no value");
    }

    @Override
    public BooleanExpression substitute(Substitutions variableValues) {
        Expression result = variableValues.getSubstitution(this);
        return result == null ? this : (BooleanExpression) result;
    }

    @Override
    public BooleanExpression precompute() {
        return this;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
    }
}
