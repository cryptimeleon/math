package de.upb.crypto.math.expressions;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.structures.Element;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Expression
 */
public interface Expression {

    /**
     * Returns an Expression where (some) variables have been substituted with the given expressions.
     * @param substitutionMap a map, where variables s will be substituted with substitutionMap(s). Output null if no substitution should take place for the given variable.
     */
    Expression substitute(Function<String, Expression> substitutionMap);

    /**
     * Returns an Expression where (some) variables have been substituted with the given expressions.
     * @param values plugs in the values from the ValueBundle into this Expression (i.e. for any variable in this expression, the variable is replaced with its value in the ValueBundle if it exists)
     */
    Expression substitute(ValueBundle values);

    default Expression substitute(String variable, Expression substitution) {
        return substitute(name -> name.equals(variable) ? substitution : null);
    }

    /**
     * Returns the set of variables the value of this expression depends on.
     */
    default Set<String> getVariables() {
        Set<String> result = new HashSet<>();
        treeWalk(node -> { if (node instanceof VariableExpression) result.add(((VariableExpression) node).getName()); });
        return result;
    }

    /**
     * Returns true if and only if this expression depends on some variable (i.e. you wouldn't be able to compute a value
     * without substituting the variable with a concrete value)
     */
    default boolean containsVariables() {
        return getVariables().isEmpty();
    }

    /**
     * Calls the given visitor in a pre-order (this, treeWalk[left child], treeWalk[right child]) fashion.
     * Usual implementation (for binary nodes) is:
     * @Override
     *     public void treeWalk(Consumer<Expression> visitor) {
     *         visitor.accept(this);
     *         child0.treeWalk(visitor);
     *         child1.treeWalk(visitor);
     *     }
     *
     * By contract, implementing objects should at least regard dependent variables as child VariableExpressions.
     */
    void treeWalk(Consumer<Expression> visitor);

    /**
     * Returns an equivalent expression that should be faster to evaluate
     * than the original (at the cost of some pre-computation done during this method call).
     *
     * This is intended to use with expressions that contain variables (and some concrete values). Typical use:
     * Expression precomputed = expr.precompute();
     * // ... some time passes, then:
     * precomputed.substitute(valuesForVariables).evaluate();
     */
    Expression precompute();
}
