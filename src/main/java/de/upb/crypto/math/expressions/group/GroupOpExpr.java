package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link GroupElementExpression} representing the group operation applied to two group element expressions.
 */
public class GroupOpExpr extends GroupElementExpression {
    private final GroupElementExpression lhs, rhs;

    public GroupOpExpr(GroupElementExpression lhs, GroupElementExpression rhs) {
        super(lhs.getGroup() != null ? lhs.getGroup() : rhs.getGroup());
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public GroupElement evaluate() {
        return lhs.evaluate().op(rhs.evaluate());
    }

    @Override
    public GroupElement evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        return lhs.evaluate(substitutions).op(rhs.evaluate(substitutions));
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    @Override
    public GroupElementExpression substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return lhs.substitute(substitutions).op(rhs.substitute(substitutions));
    }

    @Override
    protected GroupOpExpr linearize(ExponentExpr exponent) {
        GroupOpExpr lhsLinear = lhs.linearize(exponent);
        GroupOpExpr rhsLinear = rhs.linearize(exponent);
        return new GroupOpExpr(
                lhsLinear.getLhs().evaluate().op(rhsLinear.getLhs().evaluate()).expr(), //multiply the two y
                lhsLinear.getRhs().op(rhsLinear.getRhs()) //multiply the two products that contain variables
        );
    }

    /**
     * Retrieves the left hand side of this group operation expression.
     */
    public GroupElementExpression getLhs() {
        return lhs;
    }

    /**
     * Retrieves the right hand side of this group operation expression.
     */
    public GroupElementExpression getRhs() {
        return rhs;
    }
}
