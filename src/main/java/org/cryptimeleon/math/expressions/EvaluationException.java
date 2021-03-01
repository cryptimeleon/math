package org.cryptimeleon.math.expressions;

/**
 * An exception thrown if evaluation of an expressions fails due to some reason.
 */
public class EvaluationException extends RuntimeException {
    /**
     * Initializes the exception with some information.
     * @param expr the {@code Expression} that could not be evaluated
     * @param message a message for the user describing the reason for the evaluation failure
     */
    public EvaluationException(Expression expr, String message) {
        super("Cannot evaluate expression "+expr.toString() + ". Reason: " + message);
    }
}
