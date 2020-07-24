package de.upb.crypto.math.expressions;

import javassist.expr.Expr;
import org.graalvm.compiler.lir.Variable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Expression
 */
public interface Expression {

    /**
     * Returns an Expression where (some) variables have been substituted with the values given in the ValueBundle.
     */
    default Expression substitute(ValueBundle values) {
        return substitute(values::getSubstitution);
    }

    default Expression substitute(String variable, Expression substitution) {
        return substitute((VariableExpression expr) -> expr.getName().equals(variable) ? substitution : null);
    }

    Expression substitute(Function<VariableExpression, ? extends Expression> substitutions);

    Object evaluate();

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
     * Returns true if and only if this expression depends on some variable (i.e. you wouldn't be able to compute a value
     * without substituting the variable with a concrete value)
     */
    default boolean containsVariables() {
        return !getVariables().isEmpty();
    }

    /**
     * Calls the given visitor in a pre-order (this, treeWalk[left child], treeWalk[right child]) fashion.
     * Usual implementation (for binary nodes) is:
     *
     *     public void treeWalk(Consumer<Expression> visitor) {
     *         visitor.accept(this);
     *         forEachChild(Expression::treeWalk)
     *     }
     *
     * By contract, implementing objects should at least regard dependent variables as child VariableExpressions.
     */
    default void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        forEachChild(expr -> expr.treeWalk(visitor));
    }

    /**
     * Calls action on every (direct) child of this expression.
     * For example, for a GroupOpExpr, this would call action on the left- and right-hand-side and then return.
     */
    void forEachChild(Consumer<Expression> action);

    /**
     * Calls the given accumulator in a pre-order (this, treeWalk[left child], treeWalk[right child]) fashion.
     * One can view the implementation as follows:
     * v = initialValue
     * v = accumulator(v, expr)
     * v = accumulator(v, [left subtree]) //recursive call
     * v = accumulator(v, [right subtree]) //recursive call
     * return v
     */
    default <T> T accumulate(BiFunction<T, Expression, T> accumulator, T initialValue) {
        ArrayList<T> value = new ArrayList<T>(1); //workaround for needing an (effectively) final variable in tree walk
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
