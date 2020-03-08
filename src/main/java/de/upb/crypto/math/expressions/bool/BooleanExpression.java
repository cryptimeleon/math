package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.ValueBundle;

import java.util.function.Function;

public interface BooleanExpression extends Expression {
    boolean evaluate();

    /**
     * Substitutes the expression and then evaluates it.
     * This may be more efficient than calling substitute(...).evaluate()
     */
    default boolean evaluate(Function<String, Expression> substitutionMap) {
        return substitute(substitutionMap).evaluate();
    }

    @Override
    BooleanExpression substitute(Function<String, Expression> substitutionMap);

    @Override
    BooleanExpression substitute(Substitutions variableValues);

    @Override
    BooleanExpression precompute();

    default BooleanExpression and(BooleanExpression rhs) {
        return new BoolAndExpr(this, rhs);
    }

    default BooleanExpression or(BooleanExpression rhs) {
        return new BoolOrExpr(this, rhs);
    }

    default BooleanExpression not() {
        return new BoolNotExpr(this);
    }
}
