package de.upb.crypto.math.expressions.evaluator.trs;

import de.upb.crypto.math.expressions.exponent.ExponentMulExpr;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupPowExpr;

import static de.upb.crypto.math.expressions.evaluator.ExponentExpressionAnalyzer.containsVariableExpr;

/**
 * Rewrites (g^x)^y as g^{x*y}.
 */
public class MergeNestedVarExpRule implements GroupExprRule {

    @Override
    public boolean isApplicable(GroupElementExpression expr) {
        if (!(expr instanceof GroupPowExpr))
            return false;

        GroupPowExpr powExpr1 = (GroupPowExpr) expr;
        if (!(powExpr1.getBase() instanceof  GroupPowExpr))
            return false;

        GroupPowExpr powExpr2 = (GroupPowExpr) powExpr1.getBase();
        return containsVariableExpr(powExpr1.getExponent()) && containsVariableExpr(powExpr2.getExponent());
    }

    @Override
    public GroupElementExpression apply(GroupElementExpression expr) {
        GroupPowExpr powExpr1 = (GroupPowExpr) expr;
        GroupPowExpr powExpr2 = (GroupPowExpr) powExpr1.getBase();
        return new GroupPowExpr(
                powExpr2.getBase(),
                new ExponentMulExpr(powExpr2.getExponent(), powExpr1.getExponent())
        );
    }
}
