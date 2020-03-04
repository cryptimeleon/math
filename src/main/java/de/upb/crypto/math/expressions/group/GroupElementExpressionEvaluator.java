package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

public interface GroupElementExpressionEvaluator {
    GroupElement evaluate(GroupElementExpression expr);
    GroupElementExpression precompute(GroupElementExpression expr);

    /**
     *
     * @param expr an expression containing, among others, expressions about this group
     * @return a precomputed version
     */
    BooleanExpression precompute(BooleanExpression expr);
}
