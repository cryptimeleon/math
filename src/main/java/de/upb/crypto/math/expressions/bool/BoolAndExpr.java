package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link BooleanExpression} representing the Boolean AND of two {@code BooleanExpression} instances.
 */
public class BoolAndExpr implements BooleanExpression {
    /**
     * The Boolean expression on the left hand side of this Boolean AND.
     */
    protected final BooleanExpression lhs;

    /**
     * The Boolean expression on the right hand side of this Boolean AND.
     */
    protected final BooleanExpression rhs;

    public BoolAndExpr(BooleanExpression lhs, BooleanExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public BooleanExpression substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return lhs.substitute(substitutions).and(rhs.substitute(substitutions));
    }

    @Override
    public Boolean evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        return lhs.evaluate(substitutions) && rhs.evaluate(substitutions);
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    /**
     * Retrieves the Boolean expression on the left hand side of this Boolean AND.
     */
    public BooleanExpression getLhs() {
        return lhs;
    }

    /**
     * Retrieves the Boolean expression on the right hand side of this Boolean AND.
     */
    public BooleanExpression getRhs() {
        return rhs;
    }
}
