package de.upb.crypto.math.expressions.evaluator.trs;

import de.upb.crypto.math.expressions.Expression;

import java.util.List;

/**
 * Class to manage application of rewriting rules to a given expression.
 * Can find the next applicable rule and apply it.
 *
 * @author Raphael Heitjohann
 */
public class RuleApplicator {

    /**
     * Rules this applicator can apply. Order determines precedence.
     */
    private List<ExprRule> rules;

    /**
     * Tracks whether a rule was applied in the last {@link RuleApplicator#applyAllRules(Expression)} call.
     */
    private boolean applied;

    public RuleApplicator(List<ExprRule> rules) {
        this.rules = rules;
        this.applied = false;
    }

    /**
     * Find the next rule that can be applied to the given expression.
     * Rules more towards the beginning of the rule list are preferred.
     * @param expr The expression to retrieve a applicable rule for.
     * @return The applicable rule. {@code null} if no applicable rules can be found.
     */
    private ExprRule getNextApplicableRule(Expression expr) {
        for (ExprRule rule : rules) {
            if (rule.isApplicable(expr))
                return rule;
        }
        return null;
    }

    /**
     * Finds and applies the next applicable rewriting rule to the given expression.
     * Does nothing if no applicable rules exist.
     * @param expr The expression to apply the rule to.
     * @return The new expression after rule application.
     */
    public Expression applyAllRules(Expression expr) {
        Expression newExpr = expr;
        while (true) {
            ExprRule applicableRule = this.getNextApplicableRule(newExpr);
            if (applicableRule == null) {
                return newExpr;
            }
            this.applied = true;
            newExpr = applicableRule.apply(newExpr);
        }
    }

    /**
     * Check whether a rule was applied in the last {@link RuleApplicator#applyAllRules(Expression)} call.
     * Resets the tracking variable after the check.
     */
    public boolean isAppliedAndReset() {
        boolean appliedTemp = applied;
        this.applied = false;
        return appliedTemp;
    }
}
