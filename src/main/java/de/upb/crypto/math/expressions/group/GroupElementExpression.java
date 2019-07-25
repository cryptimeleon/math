package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.exponent.ExponentLiteralExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.interfaces.structures.FutureGroupElement;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Map;

/**
 * {@link Expression} that evaluates to a {@link GroupElement}.
 */
public abstract class GroupElementExpression implements Expression {
    protected Group group;

    public GroupElementExpression(Group group) {
        this.group = group;
    }

    public GroupElement evaluate() {
        return group.evaluate(this);
    }

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
    public GroupElementExpression substitute(Map<String, ? extends Expression> substitutions);
}
