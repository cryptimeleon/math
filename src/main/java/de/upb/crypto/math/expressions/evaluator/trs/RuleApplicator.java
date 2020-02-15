package de.upb.crypto.math.expressions.evaluator.trs;

import de.upb.crypto.math.expressions.group.GroupElementExpression;

import java.util.List;

public class RuleApplicator {

    /**
     * Rules this applicator can apply. Order determines precedence.
     */
    private List<GroupExprRule> rules;

    private boolean applied;

    public RuleApplicator(List<GroupExprRule> rules) {
        this.rules = rules;
        this.applied = false;
    }

    private GroupExprRule getNextApplicableRule(GroupElementExpression expr) {
        for (GroupExprRule rule : rules) {
            if (rule.isApplicable(expr))
                return rule;
        }
        return null;
    }

    public GroupElementExpression applyAllRules(GroupElementExpression expr) {
        GroupElementExpression newExpr = expr;
        while (true) {
            GroupExprRule applicableRule = this.getNextApplicableRule(newExpr);
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
