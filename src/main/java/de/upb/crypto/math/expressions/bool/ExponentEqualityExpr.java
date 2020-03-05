package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;

import java.util.function.Consumer;
import java.util.function.Function;

public class ExponentEqualityExpr implements BooleanExpression {
    protected ExponentExpr lhs, rhs;

    public ExponentEqualityExpr(ExponentExpr lhs, ExponentExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean evaluate() {
        return lhs.evaluate().equals(rhs.evaluate());
    }

    @Override
    public ExponentEqualityExpr substitute(Function<String, Expression> substitutionMap) {
        return new ExponentEqualityExpr(lhs.substitute(substitutionMap), rhs.substitute(substitutionMap));
    }

    @Override
    public BooleanExpression substitute(ValueBundle variableValues) {
        return new ExponentEqualityExpr(lhs.substitute(variableValues), rhs.substitute(variableValues));
    }

    @Override
    public BooleanExpression precompute() {
        return new ExponentEqualityExpr(lhs.precompute(), rhs.precompute());
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        lhs.treeWalk(visitor);
        rhs.treeWalk(visitor);
    }
}
