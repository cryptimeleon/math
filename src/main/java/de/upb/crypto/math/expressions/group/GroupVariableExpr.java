package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.structures.groups.Group;
import de.upb.crypto.math.structures.groups.GroupElement;

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
