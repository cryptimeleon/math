package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.exponent.*;

/**
 * Utility methods for analyzing exponent expressions.
 *
 * @author Raphael Heitjohann
 */
public class ExponentExpressionAnalyzer {

    public static boolean containsVariableExpr(ExponentExpr expr) {
        if (expr instanceof ExponentConstantExpr) {
            return false;
        } else if (expr instanceof ExponentEmptyExpr) {
            return false;
        } else if (expr instanceof ExponentInvExpr) {
            ExponentInvExpr invExpr = (ExponentInvExpr) expr;
            return containsVariableExpr(invExpr.getChild());
        } else if (expr instanceof ExponentMulExpr) {
            ExponentMulExpr mulExpr = (ExponentMulExpr) expr;
            return containsVariableExpr(mulExpr.getLhs()) || containsVariableExpr(mulExpr.getRhs());
        } else if (expr instanceof ExponentNegExpr) {
            ExponentNegExpr negExpr = (ExponentNegExpr) expr;
            return containsVariableExpr(negExpr.getChild());
        } else if (expr instanceof  ExponentSumExpr) {
            ExponentSumExpr sumExpr = (ExponentSumExpr) expr;
            return containsVariableExpr(sumExpr.getLhs()) || containsVariableExpr(sumExpr.getLhs());
        } else if (expr instanceof ExponentVariableExpr) {
            return true;
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper exponent expression.");
        }
    }
}
