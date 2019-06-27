package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;

public class GroupPowExpr implements GroupElementExpression {
    protected GroupElementExpression base;
    protected ExponentExpr exponent;

    public GroupPowExpr(GroupElementExpression base, ExponentExpr exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public GroupElement evaluate() {
        return base.evaluate().pow(exponent.evaluate());
    }
}
