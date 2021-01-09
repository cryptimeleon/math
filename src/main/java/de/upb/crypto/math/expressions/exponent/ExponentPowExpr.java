package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link ExponentExpr} representing an exponentiation with both base and exponent being exponent expressions.
 */
public class ExponentPowExpr implements ExponentExpr {
    /**
     * The base expression of this exponentiation.
     */
    protected final ExponentExpr base;

    /**
     * The power expression of this exponentiation.
     */
    protected final ExponentExpr exponent;

    public ExponentPowExpr(ExponentExpr base, ExponentExpr exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    /**
     * Retrieves the base exponent expression of this exponentiation.
     */
    public ExponentExpr getBase() {
        return base;
    }

    /**
     * Retrieves the power exponent expression of this exponentiation.
     */
    public ExponentExpr getExponent() {
        return exponent;
    }

    @Override
    public BigInteger evaluate() {
        return base.evaluate().pow(exponent.evaluate().intValueExact());
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(base);
        action.accept(exponent);
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return base.evaluate(zn).pow(exponent.evaluate());
    }

    @Override
    public ExponentExpr substitute(Substitution substitutions) {
        return base.substitute(substitutions).pow(exponent.substitute(substitutions));
    }

    @Override
    public ExponentSumExpr linearize() throws IllegalArgumentException {
        if (exponent.containsVariables())
            throw new IllegalArgumentException("Cannot linearize expression a^b, where b contains variables.");
        if (base.containsVariables())
            throw new IllegalArgumentException("Cannot linearize expression a^b, where a contains variables.");

        return new ExponentSumExpr(this, new ExponentEmptyExpr());
    }

}
