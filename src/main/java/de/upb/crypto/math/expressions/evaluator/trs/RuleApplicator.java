package de.upb.crypto.math.expressions.evaluator.trs;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.group.GroupElementExpression;

import java.util.List;

public class RuleApplicator {

    /**
     * Rules this applicator can apply. Order determines precedence.
     */
    private List<ExprRule> rules;

    private boolean applied;

    public RuleApplicator(List<ExprRule> rules) {
        this.rules = rules;
        this.applied = false;
    }

    private ExprRule getNextApplicableRule(Expression expr) {
        for (ExprRule rule : rules) {
            if (rule.isApplicable(expr))
                return rule;
        }
        return null;
    }

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

    public boolean isAppliedAndReset() {
        boolean appliedTemp = applied;
        this.applied = false;
        return appliedTemp;
    }
}
