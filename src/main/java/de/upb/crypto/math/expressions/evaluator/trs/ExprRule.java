package de.upb.crypto.math.expressions.evaluator.trs;

import de.upb.crypto.math.expressions.Expression;

/**
 * Interface for rewriting rules that can be applied to expressions.
 * Any rewriting rules should implement this interface.
 */
public interface ExprRule {
    /**
     * Checks whether this term rewriting rule is applicable to the given expr
     * @param expr The expression to check for applicability of the rule.
     * @return {@code true} if the rule is applicable, {@code false} if not.
     */
    boolean isApplicable(Expression expr);

    /**
     * Applies the term rewriting rule to the given expression.
     * @return Rewritten expression
     */
    Expression apply(Expression expr);
}
