package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public BooleanExpression substitute(Substitution substitutions) {
        return lhs.substitute(substitutions).isEqualTo(rhs.substitute(substitutions));
    }

    @Override
    public Boolean evaluate(Substitution substitutions) {
        return lhs.sub(rhs).evaluate().equals(BigInteger.ZERO);
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
