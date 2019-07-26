package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.exponent.ExponentLiteralExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.interfaces.structures.FutureGroupElement;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Expression} that evaluates to a {@link GroupElement}.
 */
public abstract class GroupElementExpression implements Expression {
    /**
     * Default evaluator for this expression.
     */
    protected final GroupElementExpressionEvaluator evaluator;

    public GroupElementExpression() {this(null);}

    public GroupElementExpression(GroupElementExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public GroupElement evaluate() {
        return evaluate(evaluator);
    }

    public GroupElement evaluate(GroupElementExpressionEvaluator evaluator) {
        if (evaluator == null)
            throw new IllegalArgumentException("Trying to evaluate expression that has no evaluator attached to it");
        return evaluator.evaluate(this);
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
        return new GroupOpExpr(this, new GroupElementLiteralExpr(rhs));
    }
    public GroupElementExpression op(String rhs) {
        return new GroupOpExpr(this, new GroupVariableExpr(rhs));
    }

    public GroupElementExpression pow(ExponentExpr exponent) {
        return new GroupPowExpr(this, exponent);
    }
    public GroupElementExpression pow(BigInteger exponent) {
        return new GroupPowExpr(this, new ExponentLiteralExpr(exponent));
    }
    public GroupElementExpression pow(Zn.ZnElement exponent) {
        return new GroupPowExpr(this, new ExponentLiteralExpr(exponent.getInteger()));
    }
    public GroupElementExpression pow(String exponent) {
        return new GroupPowExpr(this, new ExponentVariableExpr(exponent));
    }

    public GroupElementExpression opPow(GroupElementExpression rhs, ExponentExpr exponentOfRhs) { //TODO more of those (overload)
        return op(rhs.pow(exponentOfRhs));
    }

    public GroupElementExpression inv() {
        return new GroupInvExpr(this);
    }

    @Override
    public abstract GroupElementExpression substitute(Map<String, ? extends Expression> substitutions);

    public GroupElementExpression substitute(String variable, Expression substitution) {
        Map<String, Expression> map = new HashMap<>();
        map.put(variable, substitution);
        return substitute(map);
    }

    public GroupElementExpressionEvaluator getDefaultEvaluator() {
        return evaluator;
    }
}
