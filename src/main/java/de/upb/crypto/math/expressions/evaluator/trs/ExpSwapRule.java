package de.upb.crypto.math.expressions.evaluator.trs;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupPowExpr;

import static de.upb.crypto.math.expressions.evaluator.ExponentExpressionAnalyzer.containsVariableExpr;

/**
 * Rewrites (g^x)^2 as (g^2)^x for better pre-evaluation since g^2 can be evaluated already.
 */
public class ExpSwapRule implements GroupExprRule {
    @Override
    public boolean isApplicable(GroupElementExpression expr) {
        if (!(expr instanceof GroupPowExpr))
            return false;
        GroupPowExpr powExpr = (GroupPowExpr) expr;
        if (containsVariableExpr(powExpr.getExponent()) || !(powExpr.getBase() instanceof GroupPowExpr)) {
            // No sense in swapping if upper exponent contains variable
            return false;
        }
        GroupPowExpr powExpr2 = (GroupPowExpr) powExpr.getBase();
        return containsVariableExpr(powExpr2.getExponent());
    }

    @Override
    public GroupElementExpression apply(GroupElementExpression expr) {
        GroupPowExpr powExpr = (GroupPowExpr) expr;
        GroupPowExpr powExpr2 = (GroupPowExpr) powExpr.getBase();
        return new GroupPowExpr(new GroupPowExpr(powExpr2.getBase(), powExpr.getExponent()), powExpr2.getExponent());
    }
}
