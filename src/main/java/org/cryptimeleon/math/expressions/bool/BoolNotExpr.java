package org.cryptimeleon.math.expressions.bool;

import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;

import java.util.function.Consumer;

/**
 * A {@link BooleanExpression} representing the Boolean NOT of a Boolean expression.
 */
public class BoolNotExpr implements BooleanExpression {
    /**
     * The Boolean expression to which this Boolean NOT is applied
     */
    protected final BooleanExpression child;

    public BoolNotExpr(BooleanExpression child) {
        this.child = child;
    }

    /**
     * Returns the Boolean expression to which this Boolean NOT is applied.
     */
    public BooleanExpression getChild() {
        return child;
    }

    @Override
    public BooleanExpression substitute(Substitution substitutions) {
        return child.substitute(substitutions).not();
    }

    @Override
    public Boolean evaluate(Substitution substitutions) {
        return !child.evaluate(substitutions);
    }

    @Override
    public LazyBoolEvaluationResult evaluateLazy(Substitution substitutions) {
        LazyBoolEvaluationResult childResult = child.evaluateLazy(substitutions);
        if (childResult.isResultKnown())
            return LazyBoolEvaluationResult.valueOf(!childResult.getResult());
        return new LazyBoolEvaluationResult() {
            @Override
            public boolean getResult() {
                return !childResult.getResult();
            }

            @Override
            boolean isResultKnown() {
                return false;
            }
        };
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(child);
    }
}
