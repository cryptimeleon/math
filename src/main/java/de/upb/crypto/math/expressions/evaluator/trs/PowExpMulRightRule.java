package de.upb.crypto.math.expressions.evaluator.trs;

import de.upb.crypto.math.expressions.exponent.ExponentMulExpr;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupPowExpr;

import static de.upb.crypto.math.expressions.evaluator.ExponentExpressionAnalyzer.containsVariableExpr;

/**
 * Rewrites something like g^{x2} to (g^2)^x. Then the pre-evaluator can evaluate g^2 already during precomputation.
 *
 * @author Raphael Heitjohann
 */
public class PowExpMulRightRule implements GroupExprRule{

    @Override
    public boolean isApplicable(GroupElementExpression expr) {
        if (!(expr instanceof GroupPowExpr))
            return false;
        GroupPowExpr powExpr = (GroupPowExpr) expr;

        if (!(powExpr.getExponent() instanceof ExponentMulExpr))
            return false;
        ExponentMulExpr mulExpr = (ExponentMulExpr) powExpr.getExponent();
        return !containsVariableExpr(mulExpr.getRhs()) && containsVariableExpr(mulExpr.getLhs());
    }

    @Override
    public GroupElementExpression apply(GroupElementExpression expr) {
        GroupPowExpr powExpr = (GroupPowExpr) expr;
        ExponentMulExpr mulExpr = (ExponentMulExpr) powExpr.getExponent();

        return new GroupPowExpr(
                new GroupPowExpr(
                        powExpr.getBase(),
                        mulExpr.getRhs()
                ),
                mulExpr.getLhs()
        );
    }
}
