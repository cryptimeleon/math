package de.upb.crypto.math.expressions.bool;

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
}
