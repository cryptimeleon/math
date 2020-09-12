package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;

import java.util.function.Consumer;
import java.util.function.Function;

public class BoolNotExpr implements BooleanExpression {
    protected BooleanExpression child;

    public BoolNotExpr(BooleanExpression child) {
        this.child = child;
    }

    public BooleanExpression getChild() {
        return child;
    }

    @Override
    public BooleanExpression substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return child.substitute(substitutions).not();
    }

    @Override
    public Boolean evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        return !child.evaluate(substitutions);
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(child);
    }
}
