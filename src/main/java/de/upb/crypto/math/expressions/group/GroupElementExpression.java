package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.bool.GroupEqualityExpr;
import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link Expression} that evaluates to a {@link GroupElement}.
 */
public abstract class GroupElementExpression implements Expression {
    /**
     * This expression evaluates to an element of this group.
     */
    protected final Group group;

    public GroupElementExpression() {this(null);}

    public GroupElementExpression(Group group) {
        this.group = group;
    }

    public GroupElement evaluate() {
        return evaluate(x -> null);
    }

    @Override
    public abstract GroupElement evaluate(Function<VariableExpression, ? extends Expression> substitutions);

    @Override
    public abstract GroupElementExpression substitute(Function<VariableExpression, ? extends Expression> substitutions);

    @Override
    public GroupElementExpression substitute(ValueBundle values) {
        return (GroupElementExpression) Expression.super.substitute(values);
    }

    @Override
    public GroupElementExpression substitute(String variable, Expression substitution) {
        return (GroupElementExpression) Expression.super.substitute(variable, substitution);
    }

    /**
     * Applies the group operation to this expression and the given expression.
     */
    public GroupElementExpression op(GroupElementExpression rhs) {
        return new GroupOpExpr(this, rhs);
    }

    /**
     * Applies the group operation to this expression and the group expression
     * implied by the given group element.
     */
    public GroupElementExpression op(GroupElement rhs) {
        return new GroupOpExpr(this, new GroupElementConstantExpr(rhs));
    }
    /**
     * Applies the group operation to this expression and the variable expression implied by the given variable name.
     */
    public GroupElementExpression op(String rhs) {
        return new GroupOpExpr(this, new GroupVariableExpr(rhs));
    }

    /**
     * Raises this expression to the given power.
     * @param exponent the power in form of an {@link ExponentExpr}
     * @return an expression representing this expression raised to the given power
     */
    public GroupElementExpression pow(ExponentExpr exponent) {
        return new GroupPowExpr(this, exponent);
    }

    /**
     * Raises this expression to the given power.
     * @param exponent the power in form of a {@link BigInteger}
     * @return an expression representing this expression raised to the given power
     */
    public GroupElementExpression pow(BigInteger exponent) {
        return pow(new ExponentConstantExpr(exponent));
    }

    /**
     * Raises this expression to the given power.
     * @param exponent the power in form of a {@link Zn.ZnElement}
     * @return an expression representing this expression raised to the given power
     */
    public GroupElementExpression pow(Zn.ZnElement exponent) {
        return pow(new ExponentConstantExpr(exponent.getInteger()));
    }

    /**
     * Raises this expression to the given power variable.
     * @param exponent the power variable's name
     * @return an expression representing this expression raised to the given power variable
     */
    public GroupElementExpression pow(String exponent) {
        return pow(new ExponentVariableExpr(exponent));
    }

    /**
     * Applies the group operation to this expression and the given expression raised to the given power.
     */
    public GroupElementExpression opPow(GroupElementExpression rhs, ExponentExpr exponentOfRhs) {
        return op(rhs.pow(exponentOfRhs));
    }

    /**
     * Applies the group operation to this expression and the given expression raised to the given power.
     */
    public GroupElementExpression opPow(GroupElementExpression rhs, BigInteger exponentOfRhs) {
        return op(rhs.pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    /**
     * Applies the group operation to this expression and the given expression raised to the given power.
     */
    public GroupElementExpression opPow(GroupElementExpression rhs, Zn.ZnElement exponentOfRhs) {
        return op(rhs.pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    /**
     * Applies the group operation to this expression and the given expression raised to the given power variable.
     * @param exponentOfRhs the power variable's name
     */
    public GroupElementExpression opPow(GroupElementExpression rhs, String exponentOfRhs) {
        return op(rhs.pow(new ExponentVariableExpr(exponentOfRhs)));
    }

    /**
     * Applies the group operation to this expression and the given group element raised to the given power.
     */
    public GroupElementExpression opPow(GroupElement rhs, ExponentExpr exponentOfRhs) {
        return op(rhs.expr().pow(exponentOfRhs));
    }

    /**
     * Applies the group operation to this expression and the given group element raised to the given power.
     */
    public GroupElementExpression opPow(GroupElement rhs, BigInteger exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    /**
     * Applies the group operation to this expression and the given group element raised to the given power.
     */
    public GroupElementExpression opPow(GroupElement rhs, Zn.ZnElement exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    /**
     * Applies the group operation to this expression and the given group element raised to the given power variable.
     * @param exponentOfRhs the power variable's name
     */
    public GroupElementExpression opPow(GroupElement rhs, String exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentVariableExpr(exponentOfRhs)));
    }

    /**
     * Inverts this group expression.
     */
    public GroupElementExpression inv() {
        return new GroupInvExpr(this);
    }

    /**
     * Creates a {@link GroupEqualityExpr} of this expression and the argument.
     */
    public GroupEqualityExpr isEqualTo(GroupElementExpression other) {
        return new GroupEqualityExpr(this, other);
    }

    /**
     * Creates a {@link GroupEqualityExpr} of this expression and a {@link GroupElementConstantExpr} created from the
     * argument.
     */
    public GroupEqualityExpr isEqualTo(GroupElement other) {
        return new GroupEqualityExpr(this, other.expr());
    }

    /**
     * Creates a {@link GroupEqualityExpr} of this expression and a {@link GroupVariableExpr} created with the given
     * name string.
     */
    public GroupEqualityExpr isEqualTo(String other) {
        return isEqualTo(new GroupVariableExpr(other));
    }

    /**
     * Returns the group s.t. this expression evaluates to an element of this group, or null if group is unknown
     * (e.g., if expression consists only of variables)
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Prepares this expression for more efficient evaluation by sacrifing memory instead similar to
     * {@link GroupElement#precomputePow()}.
     * <p>
     * First linearizes this expression via {@link #linearize()} and then calls {@link GroupElement#precomputePow()}
     * on each {@code GroupElementConstantExpr} found in the base of each {@code GroupPowExpr} as these will be
     * exponentiated later and can use the precomputations.
     */
    public GroupElementExpression precompute() {
        GroupOpExpr linearized = linearize();
        linearized.getLhs().evaluate().computeSync();
        linearized.treeWalk(expr -> {
            if (expr instanceof GroupPowExpr) {
                GroupElementExpression base = ((GroupPowExpr) expr).getBase();
                if (base instanceof GroupElementConstantExpr)
                    ((GroupElementConstantExpr) base).value.precomputePow();
            }
        });

        return linearized;
    }

    /**
     * Returns an equivalent expression of the form \(y \cdot \prod g_i^x_i\) where \(y\) doesn't contain any variables,
     * but for every \(i\), \(g_i\) or \(x_i\) do.
     * <p>
     * The exact result is a {@code GroupOpExpr} where the left-hand-side is a {@code GroupElementConstantExpr y},
     * the right-hand-side is a tree whose inner nodes are {@code GroupOpExpr} or {@code PairingExpr}
     * and leaf nodes are {@code GroupPowExpr} (whose base is a {@code GroupConstantExpr}, a {@code GroupVariableExpr},
     * or a {@code PairingExpr} whose arguments are again linearized).
     */
    public GroupOpExpr linearize() {
        return linearize(new ExponentConstantExpr(BigInteger.ONE));
    }

    /**
     * Linearizes the expression \(\text{this}^\text{exponent}\).
     * The result must be an equivalent expression of form \(y \cdot \prod g_i^x_i\)
     * where \(y\) doesn't contain any variables, but for every \(i\), \(g_i\) or \(x_i\) do.
     */
    protected abstract GroupOpExpr linearize(ExponentExpr exponent);

    /**
     * Retrieves the order of the group element's group if possible.
     *
     * @return the order as a {@link BigInteger} if possible, else null
     */
    protected BigInteger getGroupOrderIfKnown() {
        try {
            return getGroup().size();
        } catch (UnsupportedOperationException unknownSizeException) {
            return null;
        }
    }
}
