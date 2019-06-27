package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

public class GroupEmptyExpr implements GroupElementExpression {
    protected Group group;

    public GroupEmptyExpr(Group group) {
        this.group = group;
    }

    @Override
    public GroupElement evaluate() {
        return group.getNeutralElement();
    }

    @Override
    public GroupElementExpression op(GroupElementExpression rhs) {
        return rhs;
    }

    @Override
    public GroupElementExpression op(GroupElement rhs) {
        return op(new GroupElementLiteralExpr(rhs));
    }

    @Override
    public GroupElementExpression pow(ExponentExpr exponent) {
        return this;
    }

    @Override
    public GroupElementExpression pow(BigInteger exponent) {
        return this;
    }

    @Override
    public GroupElementExpression pow(Zn.ZnElement exponent) {
        return this;
    }

    @Override
    public GroupElementExpression inv() {
        return this;
    }
}
