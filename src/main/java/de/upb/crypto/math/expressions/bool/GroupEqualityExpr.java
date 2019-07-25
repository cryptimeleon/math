package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.group.GroupElementExpression;

import java.util.Map;

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

    @Override
    public GroupEqualityExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new GroupEqualityExpr(lhs.substitute(substitutions), rhs.substitute(substitutions));
    }
}
