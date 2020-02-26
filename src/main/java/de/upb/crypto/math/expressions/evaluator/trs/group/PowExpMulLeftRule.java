package de.upb.crypto.math.expressions.evaluator.trs.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.exponent.ExponentMulExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;

import static de.upb.crypto.math.expressions.evaluator.ExponentExpressionAnalyzer.containsTypeExpr;

/**
 * Rewrites something like g^{2x} to (g^2)^x. Then the pre-evaluator can evaluate g^2 already during precomputation.
 *
 * @author Raphael Heitjohann
 */
public class PowExpMulLeftRule implements ExprRule {

    @Override
    public boolean isApplicable(Expression expr) {
        if (!(expr instanceof GroupPowExpr))
            return false;
        GroupPowExpr powExpr = (GroupPowExpr) expr;

        if (!(powExpr.getExponent() instanceof ExponentMulExpr))
            return false;
        ExponentMulExpr mulExpr = (ExponentMulExpr) powExpr.getExponent();
        return !containsTypeExpr(mulExpr.getLhs(), ExponentVariableExpr.class)
                && containsTypeExpr(mulExpr.getRhs(), ExponentVariableExpr.class);
    }

    @Override
    public Expression apply(Expression expr) {
        GroupPowExpr powExpr = (GroupPowExpr) expr;
        ExponentMulExpr mulExpr = (ExponentMulExpr) powExpr.getExponent();

        return new GroupPowExpr(
                new GroupPowExpr(
                        powExpr.getBase(),
                        mulExpr.getLhs()
                ),
                mulExpr.getRhs()
        );
    }
}
