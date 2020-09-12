package de.upb.crypto.math.expressions;

public class EvaluationException extends RuntimeException {
    public EvaluationException(Expression expr, String message) {
        super("Cannot evaluate expression "+expr.toString() + ". Reason: " + message);
    }
}
