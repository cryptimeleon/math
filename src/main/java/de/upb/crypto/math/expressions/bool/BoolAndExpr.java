package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;

import java.util.function.Consumer;
import java.util.function.Function;

public class BoolAndExpr implements BooleanExpression {
    protected BooleanExpression lhs, rhs;

    public BoolAndExpr(BooleanExpression lhs, BooleanExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public BooleanExpression substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return lhs.substitute(substitutions).and(rhs.substitute(substitutions));
    }

    @Override
    public Boolean evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        return lhs.evaluate(substitutions) && rhs.evaluate(substitutions);
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    public BooleanExpression getLhs() {
        return lhs;
    }

    public BooleanExpression getRhs() {
        return rhs;
    }
}
