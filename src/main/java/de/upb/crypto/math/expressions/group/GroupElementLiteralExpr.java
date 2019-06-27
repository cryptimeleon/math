package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.interfaces.structures.GroupElement;

public class GroupElementLiteralExpr implements GroupElementExpression {
    GroupElement value;

    public GroupElementLiteralExpr(GroupElement value) {
        this.value = value;
    }

    @Override
    public GroupElement evaluate() {
        return value;
    }
}
