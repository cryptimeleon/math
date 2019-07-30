package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;

public class GroupVariableExpr extends GroupElementExpression implements VariableExpression {
    protected final String name;

    public GroupVariableExpr(@Nonnull String name) {
        this.name = name;
    }

    @Override
    public GroupElement evaluate() {
        return evaluateNaive();
    }

    @Override
    public GroupElement evaluateNaive() {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    public GroupElementExpression substitute(Map<String, ? extends Expression> substitutions) {
        if (substitutions.containsKey(name))
            return (GroupElementExpression) substitutions.get(name);
        else
            return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
    }
}
