package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupElementExpressionEvaluator;
import de.upb.crypto.math.interfaces.structures.GroupElement;

public class NaiveGroupElementExpressionEvaluator implements GroupElementExpressionEvaluator {

    @Override
    public GroupElement evaluate(GroupElementExpression expr) {
        return expr.evaluateNaive();
    }

    @Override
    public GroupElementExpression precompute(GroupElementExpression expr) {
        return expr;
    }

    @Override
    public BooleanExpression precompute(BooleanExpression expr) {
        return expr;
    }
}
