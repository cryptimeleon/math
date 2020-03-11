package de.upb.crypto.math.expressions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Expression
 */
public interface Expression {

    /**
     * Returns an Expression where (some) variables have been substituted with other given expressions.
     * One example use is with ValueBundle. substitute(valueBundle) inserts concrete values from the ValueBundle into variables.
     */
    Expression substitute(Substitutions values);

    default Expression substitute(String variable, Expression substitution) {
        return substitute(expr -> expr.getName().equals(variable) ? substitution : null);
    }

    /**
     * Returns the set of variables the value of this expression depends on.
     */
    default Set<VariableExpression> getVariables() {
        Set<VariableExpression> result = new HashSet<>();
        treeWalk(node -> { if (node instanceof VariableExpression) result.add(((VariableExpression) node)); });
        return result;
    }

    /**
     * Returns true if and only if this expression depends on some variable (i.e. you wouldn't be able to compute a value
     * without substituting the variable with a concrete value)
     */
    default boolean containsVariables() {
        return !getVariables().isEmpty();
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
     * Calls the given accumulator in a pre-order (this, treeWalk[left child], treeWalk[right child]) fashion.
     * One can view the implementation as follows:
     * v = initialValue
     * v = accumulator(v, expr)
     * v = accumulator(v, [left subtree]) //recursive call
     * v = accumulator(v, [right subtree]) //recursive call
     *
     */
    default <T> T treeWalk(BiFunction<T, Expression, T> accumulator, T initialValue) {
        ArrayList<T> value = new ArrayList<T>(); //workaround for needing an (effectively) final variable in tree walk
        value.add(initialValue);
        treeWalk(expr -> value.set(0, accumulator.apply(value.get(0), expr)));
        return value.get(0);
    }

    /**
     * Checks if an expression fulfilling the given predicate is contained in this expression.
     */
    default boolean containsExprMatchingPredicate(Predicate<Expression> predicate) { //TODO can be more efficiently implemented (end evaluation as soon as a predicate match has been found)
        return treeWalk((Boolean hasPredicateExpr, Expression expr) -> predicate.test(expr) || hasPredicateExpr, false);
    }

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
