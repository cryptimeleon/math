package de.upb.crypto.math.expressions.evaluator.trs;

import de.upb.crypto.math.expressions.group.GroupElementExpression;

/**
 * Interface for term rewriting rules.
 *
 * @author Raphael Heitjohann
 */
public interface GroupExprRule {
    /**
     * Checks whether this term rewriting rule is applicable to the given expr
     * @param expr The expression to check for applicability of the rule.
     * @return Whether the rule is applicable.
     */
    boolean isApplicable(GroupElementExpression expr);

    /**
     * Applies the term rewriting rule to the given expression.
     * @return Rewritten expression
     */
    GroupElementExpression apply(GroupElementExpression expr);
}
