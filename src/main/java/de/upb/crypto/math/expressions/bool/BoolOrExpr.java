package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;

import java.util.Map;

public class BoolOrExpr implements BooleanExpression {
    BooleanExpression lhs, rhs;

    public BoolOrExpr(BooleanExpression lhs, BooleanExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean evaluate() {
        return lhs.evaluate() || rhs.evaluate();
    }

    @Override
    public BooleanExpression substitute(Map<String, ? extends Expression> substitutions) {
        return new BoolOrExpr(lhs.substitute(substitutions), rhs.substitute(substitutions));
    }
}
