package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.structures.Group;

import java.util.function.Consumer;

public class GroupEqualityExpr implements BooleanExpression {
    protected GroupElementExpression lhs, rhs;

    public GroupEqualityExpr(GroupElementExpression lhs, GroupElementExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public GroupElementExpression getLhs() {
        return lhs;
    }

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
