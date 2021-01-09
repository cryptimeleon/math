package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.bool.GroupEqualityExpr;
import de.upb.crypto.math.expressions.exponent.BasicNamedExponentVariableExpr;
import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

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
    default GroupElementExpression pow(Zn.ZnElement exponent) {
        return pow(new ExponentConstantExpr(exponent.getInteger()));
    }
    default GroupElementExpression pow(String exponent) {
        return pow(new BasicNamedExponentVariableExpr(exponent));
    }

    default GroupElementExpression opPow(GroupElementExpression rhs, ExponentExpr exponentOfRhs) {
        return op(rhs.pow(exponentOfRhs));
    }

    default GroupElementExpression opPow(GroupElementExpression rhs, BigInteger exponentOfRhs) {
        return op(rhs.pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    default GroupElementExpression opPow(GroupElementExpression rhs, Zn.ZnElement exponentOfRhs) {
        return op(rhs.pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    default GroupElementExpression opPow(GroupElementExpression rhs, String exponentOfRhs) {
        return op(rhs.pow(new BasicNamedExponentVariableExpr(exponentOfRhs)));
    }

    default GroupElementExpression opPow(GroupElement rhs, ExponentExpr exponentOfRhs) {
        return op(rhs.expr().pow(exponentOfRhs));
    }

    default GroupElementExpression opPow(GroupElement rhs, BigInteger exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    default GroupElementExpression opPow(GroupElement rhs, Zn.ZnElement exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentConstantExpr(exponentOfRhs)));
    }

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
     * Returns the group s.t. this expression evaluates to an element of this group, or null if group is unknown
     * (e.g., if expression consists only of variables)
     */
    Group getGroup();
    /**
     * Prepares this expression for more efficient evaluation by sacrifing memory instead similar to
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
     * Returns an equivalent expression of the form y * f(groupVariables, exponentVariables), where y is constant (no variables), and the expression f is linear, which means that
     * f(groupVariables, exponentVariables) * f(groupVariables2, exponentVariables2) = f(groupVariables * groupVariables2, exponentVariables + exponentVariables2)
     *
     * The exact result is a GroupOpExpr
     * where the left-hand-side y has !y.containsVariables(),
     * the right-hand-side is linear
     *
     * @throws IllegalArgumentException if it's not possible to form the desired output (e.g., the input is something like g^(x_1 * x_2) for variables x_1, x_2).
     */
    GroupOpExpr linearize() throws IllegalArgumentException;

    /**
     * Returns an equivalent expression of the form y * prod(g_i^x_i), where y doesn't contain any variables.
     *
     * The exact result is a GroupOpExpr
     * where the left-hand-side is a GroupElementConstantExpr y and the right-hand-side is an expression tree
     * where each inner nodes is a GroupOpExpr or a PairingExpr whose children are flattened.
     */
    default GroupOpExpr flatten() {
        return flatten(new ExponentConstantExpr(BigInteger.ONE));
    }
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


    /**
     * Linearizes the expression this^exponent.
     */
    GroupOpExpr flatten(ExponentExpr exponent);
}
