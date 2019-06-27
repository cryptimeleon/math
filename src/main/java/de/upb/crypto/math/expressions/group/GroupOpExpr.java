package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.interfaces.structures.GroupElement;

public class GroupOpExpr implements GroupElementExpression {
    protected GroupElementExpression lhs, rhs;

    public GroupOpExpr(GroupElementExpression lhs, GroupElementExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public GroupElement evaluate() {
        return lhs.evaluate().op(rhs.evaluate());
    }
}
