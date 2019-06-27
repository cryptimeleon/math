package de.upb.crypto.math.expressions.group;


import de.upb.crypto.math.interfaces.structures.GroupElement;

public class GroupInvExpr implements GroupElementExpression {
    protected GroupElementExpression base;

    public GroupInvExpr(GroupElementExpression base) {
        this.base = base;
    }

    @Override
    public GroupElement evaluate() {
        return base.evaluate().inv();
    }
}
