package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.util.function.Consumer;
import java.util.function.Function;

public class GroupOpExpr extends GroupElementExpression {
    protected GroupElementExpression lhs, rhs;

    public GroupOpExpr(GroupElementExpression lhs, GroupElementExpression rhs) {
        super(lhs.getGroup() != null ? lhs.getGroup() : rhs.getGroup());
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public GroupElement evaluateNaive() {
        return lhs.evaluateNaive().op(rhs.evaluateNaive());
    }

    @Override
    public GroupOpExpr substitute(Function<String, Expression> substitutionMap) {
        return new GroupOpExpr(lhs.substitute(substitutionMap), rhs.substitute(substitutionMap));
    }

    @Override
    public GroupOpExpr substitute(ValueBundle variableValues) {
        return new GroupOpExpr(lhs.substitute(variableValues), rhs.substitute(variableValues));
    }

    public GroupElementExpression getLhs() {
        return lhs;
    }

    public GroupElementExpression getRhs() {
        return rhs;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        lhs.treeWalk(visitor);
        rhs.treeWalk(visitor);
    }
}
