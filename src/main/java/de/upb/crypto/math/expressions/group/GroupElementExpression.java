package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.exponent.ExponentLiteralExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.interfaces.structures.FutureGroupElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

/**
 * {@link Expression} that evaluates to a {@link GroupElement}.
 */
public interface GroupElementExpression extends Expression {
    GroupElement evaluate();

    default FutureGroupElement evaluateAsync() {
        return new FutureGroupElement(this::evaluate);
    }

    default GroupElementExpression op(GroupElementExpression rhs) {
        return new GroupOpExpr(this, rhs);
    }
    default GroupElementExpression op(GroupElement rhs) {
        return new GroupOpExpr(this, new GroupElementLiteralExpr(rhs));
    }
    default GroupElementExpression op(String rhs) {
        return new GroupOpExpr(this, new GroupVariableExpr(rhs));
    }

    default GroupElementExpression pow(ExponentExpr exponent) {
        return new GroupPowExpr(this, exponent);
    }
    default GroupElementExpression pow(BigInteger exponent) {
        return new GroupPowExpr(this, new ExponentLiteralExpr(exponent));
    }
    default GroupElementExpression pow(Zn.ZnElement exponent) {
        return new GroupPowExpr(this, new ExponentLiteralExpr(exponent.getInteger()));
    }
    default GroupElementExpression pow(String exponent) {
        return new GroupPowExpr(this, new ExponentVariableExpr(exponent));
    }

    default GroupElementExpression inv() {
        return new GroupInvExpr(this);
    }
}
