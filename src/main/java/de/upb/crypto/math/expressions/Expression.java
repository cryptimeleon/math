package de.upb.crypto.math.expressions;

import de.upb.crypto.math.expressions.group.GroupOpExpr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An expression is a tree of expressions that can be evaluated if it contains no variables.
 * If variables are contained in the expression, they have to be substituted with actual values first.
 */
public interface Expression {

    /**
     * Returns an Expression where (some) variables have been substituted with the values given in the ValueBundle.
     */
    default Expression substitute(ValueBundle values) {
        return substitute(values::getSubstitution);
    }

    /**
     * Substitute a specific variable with the given expression.
     *
     * @param variable the variable to replace
     * @param substitution the expression to substitute
     * @return the expression after substitution
     */
    default Expression substitute(String variable, Expression substitution) {
        return substitute((VariableExpression expr) -> expr.getName().equals(variable) ? substitution : null);
    }

    /**
     * Substitutes variables using the given function.
     *
     * @param substitutions a function mapping variable expressions to their replacement expressions
     * @return the expression after substitution
     */
    Expression substitute(Function<VariableExpression, ? extends Expression> substitutions);

    /**
     * Evaluates the expression.
     *
     * @return the result of evaluation
     */
    Object evaluate();

    /**
     * Evaluates the expression after substituting contained variables.
     *
     * @param substitutions a function mapping variables to expressions that can be evaluated
     * @return the result of evaluation
     */
    Object evaluate(Function<VariableExpression, ? extends Expression> substitutions);

    /**
     * Returns the set of variables the value of this expression depends on.
     */
    default Set<VariableExpression> getVariables() {
        Set<VariableExpression> result = new HashSet<>();
        treeWalk(node -> { if (node instanceof VariableExpression) result.add(((VariableExpression) node)); });
        return result;
    }

    /**
     * Returns true if and only if this expression contains variables.
     * <p>
     * This means that you wouldn't be able to evaluate the expression without substituting the variable
     * with another expression that can be evaluated.
     */
    default boolean containsVariables() {
        return !getVariables().isEmpty();
    }

    /**
     * Calls the given visitor in a pre-order (this, treeWalk[left child], treeWalk[right child]) fashion.
     * <p>
     * Usual implementation (for binary nodes) is:
     * <pre>
     *     public void treeWalk(Consumer<Expression> visitor) {
     *         visitor.accept(this);
     *         forEachChild(Expression::treeWalk)
     *     }
     * </pre>
     * By contract, implementing objects should at least regard dependent variables as child VariableExpressions.
     */
    default void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        forEachChild(expr -> expr.treeWalk(visitor));
    }

    /**
     * Calls action on every (direct) child of this expression.
     * For example, for a {@link GroupOpExpr}, this would call action on the left- and right-hand-side and then return.
     */
    void forEachChild(Consumer<Expression> action);

    /**
     * Calls the given accumulator in a pre-order (this, treeWalk[left child], treeWalk[right child]) fashion.
     * <p>
     * One can view the implementation as follows:
     * <pre>
     * v = initialValue
     * v = accumulator(v, expr)
     * v = accumulator(v, [left subtree]) // recursive call
     * v = accumulator(v, [right subtree]) // recursive call
     * return v
     * </pre>
     */
    default <T> T accumulate(BiFunction<T, Expression, T> accumulator, T initialValue) {
        // workaround for needing an (effectively) final variable in tree walk
        ArrayList<T> value = new ArrayList<T>(1);
        value.add(initialValue);
        treeWalk(expr -> value.set(0, accumulator.apply(value.get(0), expr)));
        return value.get(0);
    }

    /**
     * Checks if an expression fulfilling the given predicate is contained in this expression.
     */
    default boolean containsExprMatchingPredicate(Predicate<Expression> predicate) { //TODO can be more efficiently implemented (end evaluation as soon as a predicate match has been found)
        return accumulate((Boolean hasPredicateExpr, Expression expr) -> predicate.test(expr) || hasPredicateExpr, false);
    }
}
