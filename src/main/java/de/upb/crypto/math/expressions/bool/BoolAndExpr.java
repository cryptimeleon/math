package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;

import java.util.Map;

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
    public BoolAndExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new BoolAndExpr(lhs.substitute(substitutions), rhs.substitute(substitutions));
    }
}
