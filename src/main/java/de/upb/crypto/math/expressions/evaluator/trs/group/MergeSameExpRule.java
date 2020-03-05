package de.upb.crypto.math.expressions.evaluator.trs.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.group.GroupOpExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;

/**
 * Rewrites g_1^x * g_2^x as (g_1 * g_2)^x to save an exponentiation.
 *
 * TODO: WIP. Not yet clear how to compare exponents for equality if they contain a variable.
 */
public class MergeSameExpRule implements ExprRule {
    @Override
    public boolean isApplicable(Expression expr) {
        if (!(expr instanceof GroupOpExpr))
            return false;

        GroupOpExpr opExpr = (GroupOpExpr) expr;

        if (!(opExpr.getLhs() instanceof GroupPowExpr) || !(opExpr.getRhs() instanceof GroupPowExpr))
            return false;

        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Expression apply(Expression expr) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
