package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.group.GroupElementExpression;

public class GroupEqualityExpr implements BooleanExpression {
    protected GroupElementExpression lhs, rhs;

    public GroupEqualityExpr(GroupElementExpression lhs, GroupElementExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean evaluate() {
        return lhs.evaluate().equals(rhs.evaluate());
    }
}
