package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.exponent.*;

/**
 * Utility methods for analyzing exponent expressions.
 *
 * @author Raphael Heitjohann
 */
public class ExponentExpressionAnalyzer {

    public static boolean containsTypeExpr(ExponentExpr expr, Class<?> expType) {
        if (expType == null) {
            return false;
        }
        if (expType.isInstance(expr)) {
            return true;
        }
        if (expr instanceof ExponentConstantExpr) {
            return false;
        } else if (expr instanceof ExponentEmptyExpr) {
            return false;
        } else if (expr instanceof ExponentInvExpr) {
            ExponentInvExpr invExpr = (ExponentInvExpr) expr;
            return containsTypeExpr(invExpr.getChild(), expType);
        } else if (expr instanceof ExponentMulExpr) {
            ExponentMulExpr mulExpr = (ExponentMulExpr) expr;
            return containsTypeExpr(mulExpr.getLhs(), expType) || containsTypeExpr(mulExpr.getRhs(), expType);
        } else if (expr instanceof ExponentNegExpr) {
            ExponentNegExpr negExpr = (ExponentNegExpr) expr;
            return containsTypeExpr(negExpr.getChild(), expType);
        } else if (expr instanceof  ExponentSumExpr) {
            ExponentSumExpr sumExpr = (ExponentSumExpr) expr;
            return containsTypeExpr(sumExpr.getLhs(), expType) || containsTypeExpr(sumExpr.getLhs(), expType);
        } else if (expr instanceof ExponentVariableExpr) {
            return false;
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper exponent expression.");
        }
    }
}
