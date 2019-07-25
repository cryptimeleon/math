package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.util.Map;

public class GroupElementLiteralExpr implements GroupElementExpression {
    protected GroupElement value;

    public GroupElementLiteralExpr(GroupElement value) {
        this.value = value;
    }

    @Override
    public GroupElement evaluate() {
        return value;
    }

    @Override
    public GroupElementLiteralExpr substitute(Map<String, ? extends Expression> substitutions) {
        return this;
    }
}
