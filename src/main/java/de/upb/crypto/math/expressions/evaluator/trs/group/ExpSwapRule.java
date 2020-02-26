package de.upb.crypto.math.expressions.evaluator.trs.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;

import static de.upb.crypto.math.expressions.evaluator.ExponentExpressionAnalyzer.containsTypeExpr;

/**
 * Rewrites (g^x)^2 as (g^2)^x for better pre-evaluation since g^2 can be evaluated already.
 */
public class ExpSwapRule implements ExprRule {
    @Override
    public boolean isApplicable(Expression expr) {
        if (!(expr instanceof GroupPowExpr))
            return false;
        GroupPowExpr powExpr = (GroupPowExpr) expr;
        if (containsTypeExpr(powExpr.getExponent(), ExponentVariableExpr.class)
                || !(powExpr.getBase() instanceof GroupPowExpr)) {
            // No sense in swapping if upper exponent contains variable
            return false;
        }
        GroupPowExpr powExpr2 = (GroupPowExpr) powExpr.getBase();
        return containsTypeExpr(powExpr2.getExponent(), ExponentVariableExpr.class);
    }

    @Override
    public Expression apply(Expression expr) {
        GroupPowExpr powExpr = (GroupPowExpr) expr;
        GroupPowExpr powExpr2 = (GroupPowExpr) powExpr.getBase();
        return new GroupPowExpr(new GroupPowExpr(powExpr2.getBase(), powExpr.getExponent()), powExpr2.getExponent());
    }
}
