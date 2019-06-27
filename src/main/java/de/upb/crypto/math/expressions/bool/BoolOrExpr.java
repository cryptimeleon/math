package de.upb.crypto.math.expressions.bool;

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
}
