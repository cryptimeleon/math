package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.util.Map;

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

    @Override
    public GroupPowExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new GroupPowExpr(base.substitute(substitutions), exponent.substitute(substitutions));
    }
}
