package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.exponent.*;

/**
 * Utility methods for analyzing exponent expressions.
 *
 * @author Raphael Heitjohann
 */
public class ExponentExpressionAnalyzer {

    public static boolean containsTypeExpr(ExponentExpr expr, Class<?> expType) {
        return expr.containsExprMatchingPredicate(ex -> expType != null && expType.isInstance(ex));
    }
}
