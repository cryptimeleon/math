package org.cryptimeleon.math.expressions.group;

import org.cryptimeleon.math.expressions.EvaluationException;
import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.expressions.VariableExpression;
import org.cryptimeleon.math.expressions.exponent.ExponentExpr;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;

import java.util.function.Consumer;
/**
 * A {@link GroupElementExpression} representing a variable which does not have a known value at time of creation.
 */
public interface GroupVariableExpr extends GroupElementExpression, VariableExpression {
    @Override
    default GroupElement evaluate() {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    default GroupElement evaluate(Substitution substitutions) {
        GroupElementExpression substitution = (GroupElementExpression) substitutions.getSubstitution(this);
        if (substitution == null)
            throw new EvaluationException(this, "Variable cannot be evaluated");
        return substitution.evaluate();
    }

    @Override
    default GroupElementExpression substitute(Substitution substitutions) {
        Expression replacement = substitutions.getSubstitution(this);
        if (replacement != null)
            return (GroupElementExpression) replacement;
        return this;
    }

    @Override
    default Group getGroup() {
        return null; //unknown
    }

    @Override
    default GroupOpExpr flatten(ExponentExpr exponent) {
        return new GroupOpExpr(new GroupEmptyExpr(getGroup()), this.pow(exponent));
    }

    @Override
    default void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }

    @Override
    default GroupOpExpr linearize() throws IllegalArgumentException {
        return new GroupOpExpr(new GroupEmptyExpr(), this);
    }
}
