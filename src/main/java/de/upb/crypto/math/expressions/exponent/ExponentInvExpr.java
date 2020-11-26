package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link ExponentExpr} representing the multiplicative inversion of an exponent expression.
 */
public class ExponentInvExpr implements ExponentExpr {
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
    public ExponentExpr substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return child.substitute(substitutions).invert();
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(child);
    }
}
