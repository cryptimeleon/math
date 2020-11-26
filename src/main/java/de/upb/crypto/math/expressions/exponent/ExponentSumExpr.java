package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link ExponentExpr} representing the sum of two exponent expressions.
 */
public class ExponentSumExpr implements ExponentExpr {
    private final ExponentExpr lhs, rhs;

    public ExponentSumExpr(ExponentExpr lhs, ExponentExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * Retrieves the left hand of this sum.
     */
    public ExponentExpr getLhs() {
        return lhs;
    }

    /**
     * Retrieves the right hand of this sum.
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
    public ExponentExpr substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return lhs.substitute(substitutions).add(rhs.substitute(substitutions));
    }

}
