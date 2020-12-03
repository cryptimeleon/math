package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public ExponentExpr substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return lhs.substitute(substitutions).mul(rhs.substitute(substitutions));
    }
}
