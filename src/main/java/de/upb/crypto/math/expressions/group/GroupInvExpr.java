package de.upb.crypto.math.expressions.group;


import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.util.Map;

public class GroupInvExpr implements GroupElementExpression {
    protected GroupElementExpression base;

    public GroupInvExpr(GroupElementExpression base) {
        this.base = base;
    }

    @Override
    public GroupElement evaluate() {
        return base.evaluate().inv();
    }

    @Override
    public GroupInvExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new GroupInvExpr(base.substitute(substitutions));
    }
}
