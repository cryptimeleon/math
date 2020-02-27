package de.upb.crypto.math.expressions.evaluator.trs.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.bool.GroupEqualityExpr;
import de.upb.crypto.math.expressions.evaluator.GroupElementExpressionAnalyzer;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.group.*;

/**
 * Rewrites x^a = y^b as x^a * y^{-b} = 1 to enable use of a multi-exponentiation.
 * Only applied if the right side is not already 1, and
 * at least one side must also contain a constant group element,
 * else we dont know the group to use for the neutral element.
 */
public class MoveEqTestToLeftSideRule implements ExprRule {
    @Override
    public boolean isApplicable(Expression expr) {
        if (!(expr instanceof GroupEqualityExpr))
            return false;
        GroupEqualityExpr equalityExpr = (GroupEqualityExpr) expr;
        // GroupEmptyExpr test is to ensure this cannot be recursively applied forever
        return (equalityExpr.getRhs() instanceof GroupEmptyExpr)
                && (GroupElementExpressionAnalyzer.containsTypeExpr(equalityExpr.getLhs(), GroupElementConstantExpr.class)
                || GroupElementExpressionAnalyzer.containsTypeExpr(equalityExpr.getRhs(), GroupElementConstantExpr.class));
    }

    @Override
    public Expression apply(Expression expr) {
        GroupEqualityExpr equalityExpr = (GroupEqualityExpr) expr;

        return new GroupEqualityExpr(
                new GroupOpExpr(
                        equalityExpr.getLhs(),
                        equalityExpr.getRhs().inv()
                ),
                new GroupEmptyExpr(
                        equalityExpr.getLhs().getGroup() != null
                                ? equalityExpr.getLhs().getGroup() : equalityExpr.getRhs().getGroup()
                ) // This could have a null group if we dont have constant
        );
    }
}
