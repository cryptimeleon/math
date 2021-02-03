package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.structures.groups.GroupElement;

import java.util.function.Consumer;
/**
 * A {@link GroupElementExpression} representing the group operation applied to two group element expressions.
 */
public class GroupOpExpr extends AbstractGroupElementExpression {
    protected GroupElementExpression lhs, rhs;

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
    public GroupElement evaluate(Substitution substitutions) {
        return lhs.evaluate(substitutions).op(rhs.evaluate(substitutions));
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    @Override
    public GroupElementExpression substitute(Substitution substitutions) {
        return lhs.substitute(substitutions).op(rhs.substitute(substitutions));
    }

    @Override
    public GroupOpExpr linearize() throws IllegalArgumentException {
        GroupOpExpr lhsLinear = lhs.linearize();
        GroupOpExpr rhsLinear = rhs.linearize();

        return new GroupOpExpr(lhsLinear.getLhs().op(rhsLinear.getLhs()), lhsLinear.getRhs().op(rhsLinear.getRhs()));
    }

    @Override
    public GroupOpExpr flatten(ExponentExpr exponent) {
        GroupOpExpr lhsFlat = lhs.flatten(exponent);
        GroupOpExpr rhsLinear = rhs.flatten(exponent);
        return new GroupOpExpr(
                lhsFlat.getLhs().evaluate().op(rhsLinear.getLhs().evaluate()).expr(), //multiply the two y
                lhsFlat.getRhs().op(rhsLinear.getRhs()) //multiply the two products that contain variables
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
