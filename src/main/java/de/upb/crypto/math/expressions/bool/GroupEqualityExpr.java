package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.structures.groups.Group;

import java.util.function.Consumer;

/**
 * A {@link BooleanExpression} representing the Boolean equality "=" of two {@link GroupElementExpression} instances.
 */
public class GroupEqualityExpr implements BooleanExpression {
    /**
     * The group element expression on the left hand side of this Boolean equality.
     */
    protected GroupElementExpression lhs;

    /**
     * The group element expression on the right hand side of this Boolean equality.
     */
    protected GroupElementExpression rhs;

    public GroupEqualityExpr(GroupElementExpression lhs, GroupElementExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * Retrieves the group element expression on the left hand side of this Boolean equality.
     */
    public GroupElementExpression getLhs() {
        return lhs;
    }

    /**
     * Retrieves the group element expression on the right hand side of this Boolean equality.
     */
    public GroupElementExpression getRhs() {
        return rhs;
    }

    public Group getGroup() {
        return lhs.getGroup() == null ? rhs.getGroup() : lhs.getGroup();
    }

    @Override
    public BooleanExpression substitute(Substitution substitutions) {
        return lhs.substitute(substitutions).isEqualTo(rhs.substitute(substitutions));
    }

    @Override
    public Boolean evaluate(Substitution substitutions) {
        return lhs.evaluate(substitutions).equals(rhs.evaluate(substitutions));
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }
}
