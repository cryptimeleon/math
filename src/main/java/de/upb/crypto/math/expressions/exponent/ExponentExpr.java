package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.bool.ExponentEqualityExpr;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Function;

/**
 * An {@link Expression} that evaluates to an integer.
 */
public interface ExponentExpr extends Expression {
    @Override
    BigInteger evaluate();

    /**
     * Evaluates the expression in the given ring {@link Zn}.
     * @param zn the {@code Zn} to evaluate in
     * @return the result of evaluation as a {@link Zn.ZnElement}
     */
    Zn.ZnElement evaluate(Zn zn);

    @Override
    default BigInteger evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        return substitute(substitutions).evaluate();
    }

    /**
     * Evaluates this expression in the given {@link Zn} after substituting variables using the given substitutions.
     *
     * @param zn the ring {@code Zn} to evaluate the expression in
     * @param substitutions a function mapping variables to expressions that can be evaluated
     * @return the result of evaluating this expression as a {@link Zn.ZnElement}
     */
    default Zn.ZnElement evaluate(Zn zn, Function<VariableExpression, ? extends Expression> substitutions) {
        return substitute(substitutions).evaluate(zn);
    }

    @Override
    default ExponentExpr substitute(String variable, Expression substitution) {
        return (ExponentExpr) Expression.super.substitute(variable, substitution);
    }

    @Override
    ExponentExpr substitute(Function<VariableExpression, ? extends Expression> substitutions);

    @Override
    default ExponentExpr substitute(ValueBundle values) {
        return (ExponentExpr) Expression.super.substitute(values);
    }

    /**
     * Negates this expression.
     * @return the negated expression
     */
    default ExponentExpr negate() {
        return new ExponentNegExpr(this);
    }

    /**
     * Multiplicatively inverts this expression.
     * @return the multiplicatively inverted expression
     */
    default ExponentExpr invert() {
        return new ExponentInvExpr(this);
    }

    /**
     * Adds the given expression to this expression.
     * @param other the addend
     * @return the result of adding the two expressions
     */
    default ExponentExpr add(ExponentExpr other) {
        return new ExponentSumExpr(this, other);
    }

    /**
     * Adds an {@link ExponentVariableExpr} with the given variable name to this expression.
     * @param other the name of the variable to add
     * @return the result of adding the two expressions.
     */
    default ExponentExpr add(String other) {
        return add(new ExponentVariableExpr(other));
    }

    /**
     * Subtracts the given expression from this expression.
     * Realized by adding the negation.
     * @param other the subtrahend
     * @return the result of subtraction
     */
    default ExponentExpr sub(ExponentExpr other) {
        return add(other.negate());
    }

    /**
     * Subtracts a {@link ExponentVariableExpr} with the given variable name from this expression.
     * Realized by adding the negation.
     * @param other the name of the variable to subtract
     * @return the result of subtraction
     */
    default ExponentExpr sub(String other) {
        return sub(new ExponentVariableExpr(other));
    }

    /**
     * Multiplies this expression with the given expression.
     * @param other the factor
     * @return the result of multiplication
     */
    default ExponentExpr mul(ExponentExpr other) {
        return new ExponentMulExpr(this, other);
    }

    /**
     * Multiplies this expression with the given constant.
     * @param other the factor
     * @return the result of multiplication
     */
    default ExponentExpr mul(BigInteger other) {
        return mul(new ExponentConstantExpr(other));
    }

    /**
     * Multiplies this expression with the given constant.
     * @param other the factor
     * @return the result of multiplication
     */
    default ExponentExpr mul(long other) {
        return mul(BigInteger.valueOf(other));
    }

    /**
     * Multiplies a {@link ExponentVariableExpr} with the given variable name with this expression.
     * @param other the name of the variable to multiply with
     * @return the result of multiplication
     */
    default ExponentExpr mul(String other) {
        return mul(new ExponentVariableExpr(other));
    }

    /**
     * Raises this expression to the given power.
     * @param exponent the power
     * @return the result of the exponentiation
     */
    default ExponentExpr pow(ExponentExpr exponent) {
        return new ExponentPowExpr(this, exponent);
    }

    /**
     * Raises this expression to the given power.
     * @param exponent the power
     * @return the result of the exponentiation
     */
    default ExponentExpr pow(long exponent) {
        return pow(BigInteger.valueOf(exponent));
    }

    /**
     * Raises this expression to the given power.
     * @param exponent the power
     * @return the result of the exponentiation
     */
    default ExponentExpr pow(BigInteger exponent) {
        return pow(new ExponentConstantExpr(exponent));
    }

    /**
     * Raises this expression to the power variable with the given name.
     * @param exponent the name of the power variable
     * @return the result of the exponentiation
     */
    default ExponentExpr pow(String exponent) {
        return pow(new ExponentVariableExpr(exponent));
    }

    /**
     * Creates an {@link ExponentEqualityExpr} of this expression and the argument.
     */
    default ExponentEqualityExpr isEqualTo(ExponentExpr other) {
        return new ExponentEqualityExpr(this, other);
    }

    /**
     * Creates an {@link ExponentEqualityExpr} of this expression and the argument.
     */
    default ExponentEqualityExpr isEqualTo(Zn.ZnElement other) {
        return new ExponentEqualityExpr(this, other.asExponentExpression());
    }

    /**
     * Creates an {@link ExponentEqualityExpr} of this expression and the argument.
     */
    default ExponentEqualityExpr isEqualTo(BigInteger other) {
        return new ExponentEqualityExpr(this, new ExponentConstantExpr(other));
    }
}
