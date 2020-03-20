package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.exponent.*;

/**
 * Utility methods for analyzing exponent expressions.
 *
 * @author Raphael Heitjohann
 */
public class ExponentExpressionAnalyzer {

    /**
     * Checks whether the given expression contains an expression of a given type.
     * @param expr Expression to check.
     * @param expType The type of exponent expression to look for.
     * @return {@code true} if expression contains an expression of the given type, else {@code false}.
     */
    public static boolean containsTypeExpr(ExponentExpr expr, Class<?> expType) {
        return expr.containsExprMatchingPredicate(ex -> expType != null && expType.isInstance(ex));
    }
}
