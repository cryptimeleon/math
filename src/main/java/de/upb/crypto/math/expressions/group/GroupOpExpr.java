package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.util.Map;
import java.util.function.Consumer;

public class GroupOpExpr extends GroupElementExpression {
    protected GroupElementExpression lhs, rhs;

    public GroupOpExpr(GroupElementExpression lhs, GroupElementExpression rhs) {
        super(lhs.getDefaultEvaluator() != null ? lhs.getDefaultEvaluator() : rhs.getDefaultEvaluator());
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public GroupElement evaluateNaive() {
        return lhs.evaluateNaive().op(rhs.evaluateNaive());
    }

    @Override
    public GroupOpExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new GroupOpExpr(lhs.substitute(substitutions), rhs.substitute(substitutions));
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
