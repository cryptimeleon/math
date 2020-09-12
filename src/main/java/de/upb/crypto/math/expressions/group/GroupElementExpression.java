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
 * {@link Expression} that evaluates to a {@link GroupElement}.
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

    public GroupElementExpression op(GroupElementExpression rhs) {
        return new GroupOpExpr(this, rhs);
    }
    public GroupElementExpression op(GroupElement rhs) {
        return new GroupOpExpr(this, new GroupElementConstantExpr(rhs));
    }
    public GroupElementExpression op(String rhs) {
        return new GroupOpExpr(this, new GroupVariableExpr(rhs));
    }

    public GroupElementExpression pow(ExponentExpr exponent) {
        return new GroupPowExpr(this, exponent);
    }
    public GroupElementExpression pow(BigInteger exponent) {
        return pow(new ExponentConstantExpr(exponent));
    }
    public GroupElementExpression pow(Zn.ZnElement exponent) {
        return pow(new ExponentConstantExpr(exponent.getInteger()));
    }
    public GroupElementExpression pow(String exponent) {
        return pow(new ExponentVariableExpr(exponent));
    }

    public GroupElementExpression opPow(GroupElementExpression rhs, ExponentExpr exponentOfRhs) {
        return op(rhs.pow(exponentOfRhs));
    }

    public GroupElementExpression opPow(GroupElementExpression rhs, BigInteger exponentOfRhs) {
        return op(rhs.pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    public GroupElementExpression opPow(GroupElementExpression rhs, Zn.ZnElement exponentOfRhs) {
        return op(rhs.pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    public GroupElementExpression opPow(GroupElementExpression rhs, String exponentOfRhs) {
        return op(rhs.pow(new ExponentVariableExpr(exponentOfRhs)));
    }

    public GroupElementExpression opPow(GroupElement rhs, ExponentExpr exponentOfRhs) {
        return op(rhs.expr().pow(exponentOfRhs));
    }

    public GroupElementExpression opPow(GroupElement rhs, BigInteger exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    public GroupElementExpression opPow(GroupElement rhs, Zn.ZnElement exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    public GroupElementExpression opPow(GroupElement rhs, String exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentVariableExpr(exponentOfRhs)));
    }

    public GroupElementExpression inv() {
        return new GroupInvExpr(this);
    }

    public GroupEqualityExpr isEqualTo(GroupElementExpression other) {
        return new GroupEqualityExpr(this, other);
    }

    public GroupEqualityExpr isEqualTo(GroupElement other) {
        return new GroupEqualityExpr(this, other.expr());
    }

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
     * Returns an equivalent expression of the form y * \prod g_i^x_i, where y doesn't contain any variables, but for every i, g_i or x_i do.
     * The exact result is a GroupOpExpr
     * where the left-hand-side is a GroupElementConstantExpr y,
     * the right-hand-side is a tree whose inner nodes are GroupOpExpr or PairingExpr
     * and leaf nodes are GroupPowExpr (whose base is a GroupConstantExpr, a GroupVariableExpr, or a PairingExpr whose arguments are again linearized).
     */
    public GroupOpExpr linearize() {
        return linearize(new ExponentConstantExpr(BigInteger.ONE));
    }

    /**
     * Linearizes the expression this^exponent. The result must be of form y[no variable] * \prod g_i^x_i [variables]
     */
    protected abstract GroupOpExpr linearize(ExponentExpr exponent);

    protected BigInteger getGroupOrderIfKnown() {
        try {
            return getGroup().size();
        } catch (UnsupportedOperationException unknownSizeException) {
            return null;
        }
    }
}
