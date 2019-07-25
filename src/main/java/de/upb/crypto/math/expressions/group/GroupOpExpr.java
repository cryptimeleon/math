package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.util.Map;

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

    @Override
    public GroupOpExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new GroupOpExpr(lhs.substitute(substitutions), rhs.substitute(substitutions));
    }
}
