package de.upb.crypto.math.expressions.evaluator.trs;

import de.upb.crypto.math.expressions.Expression;

public interface ExprRule {
    /**
     * Checks whether this term rewriting rule is applicable to the given expr
     * @param expr The expression to check for applicability of the rule.
     * @return Whether the rule is applicable.
     */
    boolean isApplicable(Expression expr);

    /**
     * Applies the term rewriting rule to the given expression.
     * @return Rewritten expression
     */
    Expression apply(Expression expr);
}
