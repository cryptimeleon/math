package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;

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
    public void forEachChild(Consumer<Expression> action) {
        action.accept(child);
    }
}
