package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * An {@link ExponentExpr} represening the multiplication of two exponent expressions.
 */
public class ExponentMulExpr implements ExponentExpr {
    /**
     * The left hand side of this multiplication.
     */
    protected final ExponentExpr lhs;

    /**
     * The right hand side of this multiplication.
     */
    protected final ExponentExpr rhs;

    public ExponentMulExpr(ExponentExpr lhs, ExponentExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * Retrieves the left hand side of this multiplication.
     */
    public ExponentExpr getLhs() {
        return lhs;
    }

    /**
     * Retrieves the right hand side of this multiplication.
     */
    public ExponentExpr getRhs() {
        return rhs;
    }

    @Override
    public BigInteger evaluate() {
        return lhs.evaluate().multiply(rhs.evaluate());
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return lhs.evaluate(zn).mul(rhs.evaluate(zn));
    }

    @Override
    public ExponentExpr substitute(Substitution substitutions) {
        return lhs.substitute(substitutions).mul(rhs.substitute(substitutions));
    }

    @Override
    public ExponentSumExpr linearize() throws IllegalArgumentException {
        boolean lhsHasVariables = lhs.containsVariables();
        boolean rhsHasVariables = rhs.containsVariables();

        if (lhsHasVariables && rhsHasVariables)
            throw new IllegalArgumentException("Expression is not linear (it's of the form a*b where both a and b depend on variables)");

        if (!lhsHasVariables && !rhsHasVariables)
            return new ExponentSumExpr(this, new ExponentEmptyExpr());

        if (lhsHasVariables) { //hence rhs doesn't
            ExponentSumExpr lhsLinearized = lhs.linearize();
            return new ExponentSumExpr(lhsLinearized.getLhs().mul(rhs), lhsLinearized.getRhs().mul(rhs));
        } else { //lhs is constant, rhs isn't
            ExponentSumExpr rhsLinearized = rhs.linearize();
            return new ExponentSumExpr(lhs.mul(rhsLinearized.getLhs()), lhs.mul(rhsLinearized.getRhs()));
        }
    }
}
