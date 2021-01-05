package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link GroupElementExpression} representing a variable which does not have a known value at time of creation.
 */
public class GroupVariableExpr extends GroupElementExpression implements VariableExpression {
    protected final String name;

    public GroupVariableExpr(@Nonnull String name) {
        this.name = name;
    }

    @Override
    public GroupElement evaluate() {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    public GroupElement evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        GroupElementExpression substitution = (GroupElementExpression) substitutions.apply(this);
        if (substitution == null)
            throw new EvaluationException(this, "Variable cannot be evaluated");
        return substitution.evaluate();
    }

    @Override
    public GroupElementExpression substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        Expression replacement = substitutions.apply(this);
        if (replacement != null)
            return (GroupElementExpression) replacement;
        return this;
    }

    @Override
    protected GroupOpExpr linearize(ExponentExpr exponent) {
        return new GroupOpExpr(new GroupEmptyExpr(getGroup()), this.pow(exponent));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }
}
