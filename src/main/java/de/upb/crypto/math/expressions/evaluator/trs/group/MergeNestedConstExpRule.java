package de.upb.crypto.math.expressions.evaluator.trs.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.exponent.ExponentMulExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;

import static de.upb.crypto.math.expressions.evaluator.ExponentExpressionAnalyzer.containsTypeExpr;

/**
 * Rewrites (g^2)^3 as g^{2*3}. In other words merges nested exponentiations where exponents do not contain variables.
 */
public class MergeNestedConstExpRule implements ExprRule {

    @Override
    public boolean isApplicable(Expression expr) {
        if (!(expr instanceof GroupPowExpr))
            return false;

        GroupPowExpr powExpr1 = (GroupPowExpr) expr;
        if (!(powExpr1.getBase() instanceof  GroupPowExpr))
            return false;

        GroupPowExpr powExpr2 = (GroupPowExpr) powExpr1.getBase();
        return !containsTypeExpr(powExpr1.getExponent(), ExponentVariableExpr.class)
                && !containsTypeExpr(powExpr2.getExponent(), ExponentVariableExpr.class);
    }

    @Override
    public Expression apply(Expression expr) {
        GroupPowExpr powExpr1 = (GroupPowExpr) expr;
        GroupPowExpr powExpr2 = (GroupPowExpr) powExpr1.getBase();
        return new GroupPowExpr(
                powExpr2.getBase(),
                new ExponentMulExpr(powExpr2.getExponent(), powExpr1.getExponent())
        );
    }
}
