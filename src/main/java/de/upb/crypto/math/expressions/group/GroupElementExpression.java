package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.bool.GroupEqualityExpr;
import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.interfaces.structures.FutureGroupElement;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
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
        return evaluate(group == null ? null : group.getExpressionEvaluator());
    }

    public GroupElement evaluate(GroupElementExpressionEvaluator evaluator) {
        if (evaluator == null)
            throw new IllegalArgumentException("Trying to evaluate expression that has no known group type attached to it");
        return evaluator.evaluate(this);
    }

    public GroupElement evaluate(Function<String, Expression> substitutionMap) {
        return substitute(substitutionMap).evaluate(); //TODO implement more efficiently. Needs to be done for BooleanExpr and ExponentExpr, too.
    }

    public GroupElement evaluate(ValueBundle variableValues) {
        return substitute(variableValues).evaluate();
    }

    /**
     * Naively compute the value of this expression (without optimizations provided by the group)
     * There should usually be no good reason for procotol implementors to call this. Prefer evaluate(), which should be faster.
     *
     * @return the value of this expression
     */
    public abstract GroupElement evaluateNaive();

    public FutureGroupElement evaluateAsync() {
        return new FutureGroupElement(this::evaluate);
    }

    public  GroupElementExpression op(GroupElementExpression rhs) {
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

    public GroupElementExpression opPow(GroupElement rhs, ExponentExpr exponentOfRhs) {
        return op(rhs.expr().pow(exponentOfRhs));
    }

    public GroupElementExpression opPow(GroupElement rhs, BigInteger exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentConstantExpr(exponentOfRhs)));
    }

    public GroupElementExpression opPow(GroupElement rhs, Zn.ZnElement exponentOfRhs) {
        return op(rhs.expr().pow(new ExponentConstantExpr(exponentOfRhs)));
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

    @Override
    public abstract GroupElementExpression substitute(Function<String, Expression> substitutionMap);

    @Override
    public abstract GroupElementExpression substitute(ValueBundle variableValues);

    public GroupElementExpression substitute(String variable, Expression substitution) {
        return substitute(name -> name.equals(variable) ? substitution : null);
    }

    /**
     * Returns the group s.t. this expression evaluates to an element of this group, or null if group is unknown
     * (e.g., if expression consists only of variables)
     */
    public Group getGroup() {
        return group;
    }

    @Override
    public GroupElementExpression precompute() {
        return getGroup().getExpressionEvaluator().precompute(this);
    }

    /**
     * Outputs an equivalent expression that's of the form \prod g_i^{x_i}
     */
    /*public GroupElementExpression linearize() {
        return linearize(false, new ExponentEmptyExpr());
    }*/

    /**
     * Returns a linearized expression equivalent to this^{(-1)^inverted exponent}
     */
    //protected abstract GroupElementExpression linearize(boolean inverted, ExponentExpr exponent);
}
