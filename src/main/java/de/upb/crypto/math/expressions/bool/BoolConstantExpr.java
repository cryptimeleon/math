package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;

import java.util.function.Consumer;
import java.util.function.Function;

public class BoolConstantExpr implements BooleanExpression {
    private boolean value;

    public BoolConstantExpr(boolean value) {
        this.value = value;
    }

    @Override
    public boolean evaluate() {
        return value;
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
}
