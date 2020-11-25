package de.upb.crypto.math.expressions;

/**
 * A {@code VariableExpression} is an {@link Expression} that represents a variable with a specific name.
 */
public interface VariableExpression extends Expression {
    /**
     * Retrieves the name of this variable.
     */
    String getName();
}
