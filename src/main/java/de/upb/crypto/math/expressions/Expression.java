package de.upb.crypto.math.expressions;

import de.upb.crypto.math.interfaces.structures.Element;

import java.util.Map;

/**
 * Expression
 */
public interface Expression {
    /**
     * Returns an Expression where variables have been substituted with the given expressions.
     * @param substitutions a map, where an extry (s, e) will substite variables with name s with expression e.
     */
    Expression substitute(Map<String, ? extends Expression> substitutions);
}
