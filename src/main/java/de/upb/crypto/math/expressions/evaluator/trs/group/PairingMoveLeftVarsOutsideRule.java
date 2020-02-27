package de.upb.crypto.math.expressions.evaluator.trs.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.expressions.group.PairingExpr;

import static de.upb.crypto.math.expressions.evaluator.ExponentExpressionAnalyzer.containsTypeExpr;

/**
 * Has the purpose of moving variable exponents out of the pairing such that the pairing can be pre-evaluated.
 * Rewrites something like e(g_1^{2x}, g_2) as e(g_1, g_2)^{2x}. The constant 2 can then later be drawn back
 * into the pairing by the other rules.
 *
 * @author Raphael Heitjohann
 */
public class PairingMoveLeftVarsOutsideRule implements ExprRule {
    @Override
    public boolean isApplicable(Expression expr) {
        if (!(expr instanceof PairingExpr))
            return false;
        PairingExpr pairingExpr = (PairingExpr) expr;

        if (!(pairingExpr.getLhs() instanceof GroupPowExpr))
            return false;
        GroupPowExpr leftPowExpr = (GroupPowExpr) pairingExpr.getLhs();

        return containsTypeExpr(leftPowExpr.getExponent(), ExponentVariableExpr.class);
    }

    @Override
    public Expression apply(Expression expr) {
        PairingExpr pairingExpr = (PairingExpr) expr;
        GroupPowExpr leftPowExpr = (GroupPowExpr) pairingExpr.getLhs();

        return new GroupPowExpr(
                new PairingExpr(
                        pairingExpr.getMap(),
                        leftPowExpr.getBase(),
                        pairingExpr.getRhs()
                ),
                leftPowExpr.getExponent()
        );
    }
}
