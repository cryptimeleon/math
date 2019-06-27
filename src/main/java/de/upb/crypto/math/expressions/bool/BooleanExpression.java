package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;

public interface BooleanExpression extends Expression {
    boolean evaluate();
}
