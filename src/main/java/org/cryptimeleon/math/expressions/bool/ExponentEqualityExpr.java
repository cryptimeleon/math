package org.cryptimeleon.math.expressions.bool;

import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.expressions.exponent.ExponentExpr;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * A {@link BooleanExpression} representing the Boolean equality "=" of two {@link ExponentExpr} instances.
 */
public class ExponentEqualityExpr implements BooleanExpression {
    /**
     * The exponent expression on the left hand side of this Boolean equality.
     */
    protected final ExponentExpr lhs;

    /**
     * The exponent expression on the right hand side of this Boolean equality.
     */
    protected final ExponentExpr rhs;

    public ExponentEqualityExpr(ExponentExpr lhs, ExponentExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public ExponentEqualityExpr substitute(Substitution substitutions) {
        return lhs.substitute(substitutions).isEqualTo(rhs.substitute(substitutions));
    }

    @Override
    public Boolean evaluate(Substitution substitutions) {
        return lhs.sub(rhs).evaluate().equals(BigInteger.ZERO);
    }

    @Override
    public LazyBoolEvaluationResult evaluateLazy(Substitution substitution) {
        return evaluate(substitution) ? LazyBoolEvaluationResult.TRUE : LazyBoolEvaluationResult.FALSE;
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    /**
     * Retrieves the exponent expression on the left hand side of this Boolean equality.
     */
    public ExponentExpr getLhs() {
        return lhs;
    }

    /**
     * Retrieves the exponent expression on the right hand side of this Boolean equality.
     */
    public ExponentExpr getRhs() {
        return rhs;
    }
}
