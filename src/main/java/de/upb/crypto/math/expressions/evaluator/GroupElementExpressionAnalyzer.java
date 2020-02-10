package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.group.*;

/**
 * Utility methods for anaylzing group element expressions.
 *
 * @author Raphael Heitjohann
 */
public class GroupElementExpressionAnalyzer {

    /**
     * Checks whether expression contains a variable expression.
     * @param expr Expression to check.
     * @return true if expression contains a variable expression, else false.
     */
    public static boolean containsVariableExpr(GroupElementExpression expr) {
        if (expr instanceof GroupOpExpr) {
            GroupOpExpr opExpr = (GroupOpExpr) expr;
            return containsVariableExpr(opExpr.getLhs())
                    || containsVariableExpr(opExpr.getRhs());
        } else if (expr instanceof GroupInvExpr) {
            GroupInvExpr invExpr = (GroupInvExpr) expr;
            return containsVariableExpr(invExpr.getBase());
        } else if (expr instanceof GroupPowExpr) {
            GroupPowExpr powExpr = (GroupPowExpr) expr;
            return containsVariableExpr(powExpr.getBase()) ||
                    ExponentExpressionAnalyzer.containsVariableExpr(powExpr.getExponent());
        } else if (expr instanceof GroupElementConstantExpr) {
            return false;
        } else if (expr instanceof GroupEmptyExpr) {
            return false;
        } else if (expr instanceof PairingExpr) {
            PairingExpr pairingExpr = (PairingExpr) expr;
            return containsVariableExpr(pairingExpr.getLhs())
                    || containsVariableExpr(pairingExpr.getRhs());
        } else if (expr instanceof GroupVariableExpr) {
            return true;
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper group expression.");
        }
    }
}
