package org.cryptimeleon.math.expressions.exponent;

import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * An {@link ExponentExpr} representing the multiplicative inversion of an exponent expression.
 */
public class ExponentInvExpr implements ExponentExpr {
    /**
     * The exponent expression being inverted.
     */
    protected final ExponentExpr child;

    public ExponentInvExpr(ExponentExpr child) {
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
        throw new IllegalArgumentException("Cannot invert element over Z. Use evaluate(Zn) instead.");
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return child.evaluate(zn).inv();
    }

    @Override
    public ExponentExpr substitute(Substitution substitutions) {
        return child.substitute(substitutions).invert();
    }

    @Override
    public ExponentSumExpr linearize() throws IllegalArgumentException {
        if (child.containsVariables())
            throw new IllegalArgumentException("Cannot linearize - inversion of variables isn't linear");

        return new ExponentSumExpr(this, new ExponentEmptyExpr());
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(child);
    }
}
