package de.upb.crypto.math.expressions.evaluator.trs;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.expressions.group.PairingExpr;

import static de.upb.crypto.math.expressions.evaluator.ExponentExpressionAnalyzer.containsVariableExpr;

/**
 * Rule that moves the exponent from an exponentiation with a pairing as its base to group 1 of the pairing,
 * e.g. e(g_1, g_2)^2 -> e(g_1^2, g_2). Exponentiation in group 1 is much cheaper than in the target group.
 * It does not move variables however, as then the pairing itself cannot be pre-evaluated which is very important.
 */
public class PairingGtExpRule implements GroupExprRule {

    @Override
    public boolean isApplicable(GroupElementExpression expr) {
        if (!(expr instanceof GroupPowExpr))
            return false;

        GroupPowExpr powExpr = (GroupPowExpr) expr;

        return powExpr.getBase() instanceof PairingExpr && !containsVariableExpr(powExpr.getExponent());
    }

    @Override
    public GroupElementExpression apply(GroupElementExpression expr) {
        GroupPowExpr powExpr = (GroupPowExpr) expr;
        PairingExpr pairingExpr = (PairingExpr) powExpr.getBase();

        return new PairingExpr(
                pairingExpr.getMap(),
                new GroupPowExpr(pairingExpr.getLhs(), powExpr.getExponent()),
                pairingExpr.getRhs()
        );
    }
}
