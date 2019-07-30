package de.upb.crypto.math.expressions;

import de.upb.crypto.math.interfaces.structures.Element;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Expression
 */
public interface Expression {
    /**
     * Returns an Expression where variables have been substituted with the given expressions.
     * @param substitutions a map, where an extry (s, e) will substite variables with name s with expression e.
     */
    Expression substitute(Map<String, ? extends Expression> substitutions);

    /**
     * Returns the set of variables the value of this expression depends on.
     */
    default Set<String> getVariables() {
        Set<String> result = new HashSet<>();
        treeWalk(node -> { if (node instanceof VariableExpression) result.add(((VariableExpression) node).getName()); });
        return result;
    }

    /**
     * Calls the given visitor in a pre-order (this, treeWalk[left child], treeWalk[right child]) fashion.
     * Usual implementation (for binary nodes with children lhs, rhs) is:
     * @Override
     *     public void treeWalk(Consumer<Expression> visitor) {
     *         visitor.accept(this);
     *         lhs.treeWalk(visitor);
     *         rhs.treeWalk(visitor);
     *     }
     *
     * By contract, implementing objects should at least regard dependent variables as child VariableExpressions.
     */
    void treeWalk(Consumer<Expression> visitor);
}
