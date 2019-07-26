package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

public interface GroupElementExpressionEvaluator {
    GroupElement evaluate(GroupElementExpression expr);
    GroupElementExpression optimize(GroupElementExpression expr);
    GroupElementExpression precompute(GroupElementExpression expr);
}
