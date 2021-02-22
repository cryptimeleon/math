package org.cryptimeleon.math.expressions.exponent;

import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.expressions.VariableExpression;
import org.cryptimeleon.math.expressions.bool.ExponentEqualityExpr;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;

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
    default BigInteger evaluate(Substitution substitutions) {
        return substitute(substitutions).evaluate();
    }

    /**
     * Evaluates this expression in the given {@link Zn} after substituting variables using the given substitutions.
     *
     * @param zn the ring {@code Zn} to evaluate the expression in
     * @param substitutions a function mapping variables to expressions that can be evaluated
     * @return the result of evaluating this expression as a {@link Zn.ZnElement}
     */
    default Zn.ZnElement evaluate(Zn zn, Substitution substitutions) {
        return substitute(substitutions).evaluate(zn);
    }

    @Override
    default ExponentExpr substitute(String variable, Expression substitution) {
        return (ExponentExpr) Expression.super.substitute(variable, substitution);
    }

    @Override
    default ExponentExpr substitute(VariableExpression variable, Expression substitution) {
        return (ExponentExpr) Expression.super.substitute(variable, substitution);
    }

    @Override
    ExponentExpr substitute(Substitution substitutions);

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

    default ExponentExpr add(Zn.ZnElement other) {
        return add(other.asExponentExpression());
    }

    /**
     * Adds an {@link ExponentVariableExpr} with the given variable name to this expression.
     * @param other the name of the variable to add
     * @return the result of adding the two expressions.
     */
    default ExponentExpr add(String other) {
        return add(new BasicNamedExponentVariableExpr(other));
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

    default ExponentExpr sub(Zn.ZnElement other) {
        return sub(other.asExponentExpression());
    }

    /**
     * Subtracts a {@link ExponentVariableExpr} with the given variable name from this expression.
     * Realized by adding the negation.
     * @param other the name of the variable to subtract
     * @return the result of subtraction
     */
    default ExponentExpr sub(String other) {
        return sub(new BasicNamedExponentVariableExpr(other));
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

    default ExponentExpr mul(RingElement other) { return mul(other.asInteger()); }

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
        return mul(new BasicNamedExponentVariableExpr(other));
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

    default ExponentExpr pow(RingElement exponent) {
        return pow(exponent.asInteger());
    }

    /**
     * Raises this expression to the power variable with the given name.
     * @param exponent the name of the power variable
     * @return the result of the exponentiation
     */
    default ExponentExpr pow(String exponent) {
        return pow(new BasicNamedExponentVariableExpr(exponent));
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


    /**
     * Returns an equivalent expression of the form {@code y + f(variables)}, where {@code y} is constant (no variables),
     * and the expression {@code f} is linear.
     * Linearity means that
     * <pre>
     * f(variables) + f(variables2) = f(variables + variables2)
     * </pre>
     * The exact result is a {@code ExponentSumExpr} where the left-hand-side {@code y} fulfills
     * {@code y.containsVariables() == false} and the right-hand side is linear.
     *
     * @throws IllegalArgumentException if it's not possible to form the desired output
     * (e.g., the input is something like \(g^{x_1 \cdot x_2}\) for variables \(x_1, x_2\)).
     */
    ExponentSumExpr linearize() throws IllegalArgumentException;
}
