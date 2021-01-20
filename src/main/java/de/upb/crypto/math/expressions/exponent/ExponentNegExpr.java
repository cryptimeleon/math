package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * An {@link ExponentExpr} representing the negation of an exponent expression.
 */
public class ExponentNegExpr implements ExponentExpr {
    /**
     * The exponent expression being inverted.
     */
    protected final ExponentExpr child;

    public ExponentNegExpr(ExponentExpr child) {
        this.child = child;
    }

    /**
     * Retrieves the exponent expression being inverted.
     */
    public ExponentExpr getChild() {
        return child;
    }

    @Override
    public BigInteger evaluate() {
        return child.evaluate().negate();
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(child);
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return child.evaluate(zn).neg();
    }

    @Override
    public ExponentExpr substitute(Substitution substitutions) {
        return child.substitute(substitutions).negate();
    }

    @Override
    public ExponentSumExpr linearize() throws IllegalArgumentException {
        ExponentSumExpr childLinearized = child.linearize();
        return new ExponentSumExpr(childLinearized.getLhs().negate(), childLinearized.getRhs().negate());
    }

}
