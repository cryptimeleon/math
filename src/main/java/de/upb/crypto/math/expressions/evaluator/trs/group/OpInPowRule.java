package de.upb.crypto.math.expressions.evaluator.trs.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.evaluator.GroupElementExpressionAnalyzer;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupOpExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;

/**
 * Rewrites (g_1^x * g_2^y)^z as (g_1^x)^z * (g_2^y)^z.
 * If the exponentiations inside the larger exponentiation do not contain variables,,
 * this rewriting does not make sense as the inner one can be precomputed. Hence, the rule
 * is skipped in that case.
 */
public class OpInPowRule implements ExprRule {
    @Override
    public boolean isApplicable(Expression expr) {
        if (!(expr instanceof GroupPowExpr))
            return false;

        GroupPowExpr powExpr = (GroupPowExpr) expr;

        if (!(powExpr.getBase() instanceof GroupOpExpr))
            return false;

        // Now check that the inner exponentiations contain atleast one variable, else
        // moving the outer exponent does not make sense as the inner op can be pre-evaluated.
        GroupOpExpr opExpr = (GroupOpExpr) powExpr.getBase();
        return GroupElementExpressionAnalyzer.containsVariableExpr(opExpr.getLhs())
                || GroupElementExpressionAnalyzer.containsVariableExpr(opExpr.getRhs());
    }

    @Override
    public Expression apply(Expression expr) {
        GroupPowExpr powExpr = (GroupPowExpr) expr;
        GroupOpExpr opExpr = (GroupOpExpr) powExpr.getBase();
        return new GroupOpExpr(
                new GroupPowExpr(opExpr.getLhs(), powExpr.getExponent()),
                new GroupPowExpr(opExpr.getRhs(), powExpr.getExponent())
        );
    }
}
