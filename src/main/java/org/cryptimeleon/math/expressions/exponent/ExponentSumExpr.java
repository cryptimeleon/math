package org.cryptimeleon.math.expressions.exponent;

import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * An {@link ExponentExpr} representing the sum of two exponent expressions.
 */
public class ExponentSumExpr implements ExponentExpr {
    /**
     * The left hand side of this sum.
     */
    protected final ExponentExpr lhs;

    /**
     * The right hand side of this sum.
     */
    protected final ExponentExpr rhs;

    public ExponentSumExpr(ExponentExpr lhs, ExponentExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * Retrieves the left hand side of this sum.
     */
    public ExponentExpr getLhs() {
        return lhs;
    }

    /**
     * Retrieves the right hand side of this sum.
     */
    public ExponentExpr getRhs() {
        return rhs;
    }

    @Override
    public BigInteger evaluate() {
        return lhs.evaluate().add(rhs.evaluate());
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return lhs.evaluate(zn).add(rhs.evaluate(zn));
    }

    @Override
    public ExponentExpr substitute(Substitution substitutions) {
        return lhs.substitute(substitutions).add(rhs.substitute(substitutions));
    }

    @Override
    public ExponentSumExpr linearize() throws IllegalArgumentException {
        ExponentSumExpr lhsLinearized = lhs.linearize();
        ExponentSumExpr rhsLinearized = rhs.linearize();

        return new ExponentSumExpr(lhsLinearized.getLhs().add(rhsLinearized.getLhs()), lhsLinearized.getRhs().add(rhsLinearized.getRhs()));
    }

}
