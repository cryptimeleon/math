package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.interfaces.structures.GroupElement;

public class NaiveGroupElementExpressionEvaluator implements GroupElementExpressionEvaluator {

    @Override
    public GroupElement evaluate(GroupElementExpression expr) {
        return expr.evaluateNaive();
    }

    @Override
    public GroupElementExpression optimize(GroupElementExpression expr) {
        return expr;
    }

    @Override
    public GroupElementExpression precompute(GroupElementExpression expr) {
        return expr;
    }
}
