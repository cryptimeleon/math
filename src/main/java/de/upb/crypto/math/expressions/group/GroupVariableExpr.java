package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.util.Map;

public class GroupVariableExpr extends VariableExpression implements GroupElementExpression {
    public GroupVariableExpr(String name) {
        super(name);
    }

    @Override
    public GroupElement evaluate() {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    public GroupElementExpression substitute(Map<String, ? extends Expression> substitutions) {
        if (substitutions.containsKey(name))
            return (GroupElementExpression) substitutions.get(name);
        else
            return this;
    }
}
