package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.VariableExpression;

import java.util.Map;
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
    public BooleanExpression substitute(Function<String, Expression> substitutionMap) {
        Expression result = substitutionMap.apply(name);
        return result == null ? this : (BooleanExpression) result;
    }

    @Override
    public BooleanExpression substitute(ValueBundle variableValues) {
        Boolean result = variableValues.getBoolean(name);
        return result == null ? this : new BoolConstantExpr(result);
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
