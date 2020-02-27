package de.upb.crypto.math.expressions.evaluator.trs.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.expressions.group.PairingExpr;

import static de.upb.crypto.math.expressions.evaluator.ExponentExpressionAnalyzer.containsTypeExpr;

/**
 * Serves the purpose of moving variable exponents out of the pairing such that the pairing can be pre-evaluated.
 * Rewrites something like e(g_1, g_2^{2x}) as e(g_1, g_2)^{2x}. Then other rules can move the constant part back
 * into the pairing.
 */
public class PairingMoveRightVarsOutsideRule implements ExprRule {
    @Override
    public boolean isApplicable(Expression expr) {
        if (!(expr instanceof PairingExpr))
            return false;
        PairingExpr pairingExpr = (PairingExpr) expr;

        if (!(pairingExpr.getRhs() instanceof GroupPowExpr))
            return false;
        GroupPowExpr rightPowExpr = (GroupPowExpr) pairingExpr.getRhs();

        return containsTypeExpr(rightPowExpr.getExponent(), ExponentVariableExpr.class);
    }

    @Override
    public Expression apply(Expression expr) {
        PairingExpr pairingExpr = (PairingExpr) expr;
        GroupPowExpr rightPowExpr = (GroupPowExpr) pairingExpr.getRhs();

        return new GroupPowExpr(
                new PairingExpr(
                        pairingExpr.getMap(),
                        pairingExpr.getLhs(),
                        rightPowExpr.getBase()
                ),
                rightPowExpr.getExponent()
        );
    }
}
