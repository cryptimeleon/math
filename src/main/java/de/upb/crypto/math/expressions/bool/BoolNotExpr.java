package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;

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
    public boolean evaluate() {
        return !child.evaluate();
    }

    @Override
    public BooleanExpression substitute(Function<String, Expression> substitutionMap) {
        return new BoolNotExpr(child.substitute(substitutionMap));
    }

    @Override
    public BooleanExpression substitute(ValueBundle variableValues) {
        return new BoolNotExpr(child.substitute(variableValues));
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        child.treeWalk(visitor);
    }

    @Override
    public BooleanExpression precompute() {
        return new BoolNotExpr(child.precompute());
    }
}
