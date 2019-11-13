package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;

import java.util.function.Consumer;
import java.util.function.Function;

public class BoolEmptyExpr implements BooleanExpression {

    @Override
    public boolean evaluate() {
        throw new IllegalArgumentException("Cannot evaluate an empty expression.");
    }

    @Override
    public BooleanExpression substitute(Function<String, Expression> substitutionMap) {
        return this;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
    }

    @Override
    public BooleanExpression precompute() {
        return this;
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
