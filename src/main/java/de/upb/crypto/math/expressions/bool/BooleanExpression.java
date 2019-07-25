package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;

import java.util.Map;

public interface BooleanExpression extends Expression {
    boolean evaluate();

    @Override
    BooleanExpression substitute(Map<String, ? extends Expression> substitutions);
}
