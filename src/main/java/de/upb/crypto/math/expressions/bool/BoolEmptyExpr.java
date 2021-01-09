package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;

import java.util.function.Consumer;

public class BoolEmptyExpr implements BooleanExpression {

    @Override
    public BooleanExpression substitute(Substitution substitutions) {
        return this;
    }

    @Override
    public Boolean evaluate() {
        throw new IllegalArgumentException("Cannot evaluate an empty expression.");
    }

    @Override
    public Boolean evaluate(Substitution substitutions) {
        return evaluate();
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
