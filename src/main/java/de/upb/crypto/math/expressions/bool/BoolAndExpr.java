package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class BoolAndExpr implements BooleanExpression {
    protected BooleanExpression lhs, rhs;

    public BoolAndExpr(BooleanExpression lhs, BooleanExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean evaluate() {
        return lhs.evaluate() && rhs.evaluate();
    }

    @Override
    public BoolAndExpr substitute(Function<String, Expression> substitutionMap) {
        return new BoolAndExpr(lhs.substitute(substitutionMap), rhs.substitute(substitutionMap));
    }

    @Override
    public BooleanExpression precompute() {
        return new BoolAndExpr(lhs.precompute(), rhs.precompute()); //TODO better implementation should expose multiple "and" expressions to relevant groups
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        lhs.treeWalk(visitor);
        rhs.treeWalk(visitor);
    }
}
