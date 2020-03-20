package de.upb.crypto.math.expressions.evaluator;

import de.upb.crypto.math.expressions.group.*;

/**
 * Utility methods for analyzing group element expressions.
 *
 * @author Raphael Heitjohann
 */
public class GroupElementExpressionAnalyzer {

    /**
     * Checks whether the given expression contains an expression of a given type.
     * @param expr Expression to check.
     * @param groupType The type of group expression to look for.
     * @param expType The type of exponent expression to look for in case we reach an exponentiation.
     * @return {@code true} if expression contains an expression of either of the given types, else {@code false}.
     */
    public static boolean containsTypeExpr(GroupElementExpression expr, Class<?> groupType, Class<?> expType) {
        return expr.containsExprMatchingPredicate(ex -> groupType != null && groupType.isInstance(ex) || expType != null && expType.isInstance(ex));
    }

    /**
     * Checks whether the given expression contains an expression of a given type.
     * Does not check exponent types.
     * @param expr Expression to check.
     * @param groupType The type of group expression to look for.
     * @return {@code true} if expression contains an expression of the given type, else {@code false}.
     */
    public static boolean containsTypeExpr(GroupElementExpression expr, Class<?> groupType) {
        return containsTypeExpr(expr, groupType, null);
    }
}
