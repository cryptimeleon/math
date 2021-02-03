package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.bool.GroupEqualityExpr;
import de.upb.crypto.math.expressions.exponent.BasicNamedExponentVariableExpr;
import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.structures.groups.Group;
import de.upb.crypto.math.structures.groups.GroupElement;
import de.upb.crypto.math.structures.rings.RingElement;
import de.upb.crypto.math.structures.rings.zn.Zn;


import java.math.BigInteger;

/**
 * An {@link Expression} that evaluates to a {@link GroupElement}.
 */
public interface GroupElementExpression extends Expression {

    default GroupElement evaluate() {
        return evaluate(x -> null);
    }

    @Override
    GroupElement evaluate(Substitution substitutions);

    @Override
    GroupElementExpression substitute(Substitution substitutions);

    @Override
    default GroupElementExpression substitute(String variable, Expression substitution) {
        return (GroupElementExpression) Expression.super.substitute(variable, substitution);
    }

    @Override
    default GroupElementExpression substitute(VariableExpression variable, Expression substitution) {
        return (GroupElementExpression) Expression.super.substitute(variable, substitution);
    }

    default GroupElementExpression op(GroupElementExpression rhs) {
        return new GroupOpExpr(this, rhs);
    }
    default GroupElementExpression op(GroupElement rhs) {
        return new GroupOpExpr(this, new GroupElementConstantExpr(rhs));
    }
    default GroupElementExpression op(String rhs) {
        return new GroupOpExpr(this, new BasicNamedGroupVariableExpr(rhs));
    }

    default GroupElementExpression pow(ExponentExpr exponent) {
        return new GroupPowExpr(this, exponent);
    }
    default GroupElementExpression pow(BigInteger exponent) {
        return pow(new ExponentConstantExpr(exponent));
    }
    default GroupElementExpression pow(Long exponent) {
        return pow(new ExponentConstantExpr(exponent));
    }
    default GroupElementExpression pow(RingElement exponent) {
        return pow(new ExponentConstantExpr(exponent.asInteger()));
    }
    default GroupElementExpression pow(String exponent) {
        return pow(new BasicNamedExponentVariableExpr(exponent));
    }

    /**
     * Applies the group operation to this expression and the given expression raised to the given power.
     */
    default GroupElementExpression opPow(GroupElementExpression rhs, ExponentExpr exponentOfRhs) {
        return op(rhs.pow(exponentOfRhs));
    }

    /**
     * Applies the group operation to this expression and the given expression raised to the given power.
     */
    default GroupElementExpression opPow(GroupElementExpression rhs, BigInteger exponentOfRhs) {
        return op(rhs.pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    /**
     * Applies the group operation to this expression and the given expression raised to the given power.
     */
    default GroupElementExpression opPow(GroupElementExpression rhs, Zn.ZnElement exponentOfRhs) {
        return op(rhs.pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    /**
     * Applies the group operation to this expression and the given expression raised to the given power variable.
     * @param exponentOfRhs the power variable's name
     */
    default GroupElementExpression opPow(GroupElementExpression rhs, String exponentOfRhs) {
        return op(rhs.pow(new BasicNamedExponentVariableExpr(exponentOfRhs)));
    }

    /**
     * Applies the group operation to this expression and the given group element raised to the given power.
     */
    default GroupElementExpression opPow(GroupElement rhs, ExponentExpr exponentOfRhs) {
        return op(rhs.expr().pow(exponentOfRhs));
    }

    /**
     * Applies the group operation to this expression and the given group element raised to the given power.
     */
    default GroupElementExpression opPow(GroupElement rhs, BigInteger exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    /**
     * Applies the group operation to this expression and the given group element raised to the given power.
     */
    default GroupElementExpression opPow(GroupElement rhs, Zn.ZnElement exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    /**
     * Applies the group operation to this expression and the given group element raised to the given power variable.
     * @param exponentOfRhs the power variable's name
     */
    default GroupElementExpression opPow(GroupElement rhs, String exponentOfRhs) {
        return op(rhs.expr().pow(new BasicNamedExponentVariableExpr(exponentOfRhs)));
    }

    default GroupElementExpression inv() {
        return new GroupInvExpr(this);
    }

    default GroupEqualityExpr isEqualTo(GroupElementExpression other) {
        return new GroupEqualityExpr(this, other);
    }

    default GroupEqualityExpr isEqualTo(GroupElement other) {
        return new GroupEqualityExpr(this, other.expr());
    }

    default GroupEqualityExpr isEqualTo(String other) {
        return isEqualTo(new BasicNamedGroupVariableExpr(other));
    }

    /**
     * Returns the group such that this expression evaluates to an element of this group, or null if group is unknown
     * (for example, if expression consists only of variables).
     */
    Group getGroup();

    /**
     * Prepares this expression for more efficient evaluation by sacrificing memory instead similar to
     * {@link GroupElement#precomputePow()}.
     * <p>
     * First linearizes this expression via {@link #linearize()} and then calls {@link GroupElement#precomputePow()}
     * on each {@code GroupElementConstantExpr} found in the base of each {@code GroupPowExpr} as these will be
     * exponentiated later and can use the precomputations.
     */
    default GroupElementExpression precompute() {
        GroupOpExpr flattened = flatten();
        flattened.getLhs().evaluate().computeSync();
        flattened.treeWalk(expr -> {
            if (expr instanceof GroupPowExpr) {
                GroupElementExpression base = ((GroupPowExpr) expr).getBase();
                if (base instanceof GroupElementConstantExpr)
                    ((GroupElementConstantExpr) base).value.precomputePow();
            }
        });

        return flattened;
    }

    /**
     * Returns an equivalent expression of the form {@code y * f(groupVariables, exponentVariables)},
     * where {@code y} is constant (no variables), and the expression {@code f} is linear.
     * Linearity means that
     * <pre>
     * f(groupVariables, exponentVariables) * f(groupVariables2, exponentVariables2)
     * = f(groupVariables * groupVariables2, exponentVariables + exponentVariables2)
     * </pre>
     * The exact result is a {@code GroupOpExpr} where the left-hand-side {@code y} fulfills
     * {@code y.containsVariables() == false} and the right-hand side is linear.
     *
     * @throws IllegalArgumentException if it's not possible to form the desired output
     * (e.g., the input is something like \(g^{x_1 \cdot x_2}\) for variables \(x_1, x_2\)).
     */
    GroupOpExpr linearize() throws IllegalArgumentException;

    /**
     * Returns an equivalent expression of the form \(y \cdot \prod(g_i^{x_i})\), where \(y\) doesn't contain any variables.
     *
     * The exact result is a {@code GroupOpExpr}
     * where the left-hand side is a {@code GroupElementConstantExpr} y and the right-hand side is an expression tree
     * where each inner nodes is a {@code GroupOpExpr} or a {@code PairingExpr} whose children are flattened.
     */
    default GroupOpExpr flatten() {
        return flatten(new ExponentConstantExpr(BigInteger.ONE));
    }

    /**
     * Linearizes the expression \(\text{this}^\text{exponent}\).
     */
    GroupOpExpr flatten(ExponentExpr exponent);
}
