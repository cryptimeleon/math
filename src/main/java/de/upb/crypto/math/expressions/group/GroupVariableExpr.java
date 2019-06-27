package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.interfaces.structures.FutureGroupElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;

public class GroupVariableExpr extends VariableExpression implements GroupElementExpression {
    public GroupVariableExpr(String name) {
        super(name);
    }

    @Override
    public GroupElement evaluate() {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }
}
