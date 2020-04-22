package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.bool.ExponentEqualityExpr;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

public interface ExponentExpr extends Expression {
    BigInteger evaluate();
    Zn.ZnElement evaluate(Zn zn);

    default BigInteger evaluate(Substitutions substitutionMap) {
        return substitute(substitutionMap).evaluate();
    }

    default Zn.ZnElement evaluate(Zn zn, Substitutions substitutionMap) {
        return substitute(substitutionMap).evaluate(zn);
    }

    @Override
    ExponentExpr substitute(Substitutions variableValues);

    @Override
    ExponentExpr precompute();

    default ExponentExpr negate() {
        return new ExponentNegExpr(this);
    }

    default ExponentExpr invert() {
        return new ExponentInvExpr(this);
    }

    default ExponentExpr add(ExponentExpr other) {
        return new ExponentSumExpr(this, other);
    }

    default ExponentExpr add(String other) {
        return add(new ExponentVariableExpr(other));
    }

    default ExponentExpr sub(ExponentExpr other) {
        return add(other.negate());
    }

    default ExponentExpr sub(String other) {
        return sub(new ExponentVariableExpr(other));
    }

    default ExponentExpr mul(ExponentExpr other) {
        return new ExponentMulExpr(this, other);
    }

    default ExponentExpr mul(BigInteger other) {
        return mul(new ExponentConstantExpr(other));
    }

    default ExponentExpr mul(long other) {
        return mul(BigInteger.valueOf(other));
    }

    default ExponentExpr mul(String other) {
        return mul(new ExponentVariableExpr(other));
    }

    default ExponentExpr pow(ExponentExpr exponent) {
        return new ExponentPowExpr(this, exponent);
    }

    default ExponentExpr pow(long exponent) {
        return pow(BigInteger.valueOf(exponent));
    }

    default ExponentExpr pow(BigInteger exponent) {
        return pow(new ExponentConstantExpr(exponent));
    }

    default ExponentExpr pow(String exponent) {
        return pow(new ExponentVariableExpr(exponent));
    }

    default ExponentEqualityExpr isEqualTo(ExponentExpr other) {
        return new ExponentEqualityExpr(this, other);
    }

    default ExponentEqualityExpr isEqualTo(Zn.ZnElement other) {
        return new ExponentEqualityExpr(this, other.asExponentExpression());
    }

    default ExponentEqualityExpr isEqualTo(BigInteger other) {
        return new ExponentEqualityExpr(this, new ExponentConstantExpr(other));
    }

    default GroupElementExpression asAdditiveGroupElementExpression(Zn zn) {
        return new ZnExponentAsAdditiveGroupElemExpr(zn,this);
    }

    default GroupElementExpression asMultiplicativeGroupElementExpression(Zn zn) {
        return new ZnExponentAsMultiplicativeGroupElemExpr(zn, this);
    }
}
