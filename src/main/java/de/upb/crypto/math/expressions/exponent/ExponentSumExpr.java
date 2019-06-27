package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

public class ExponentSumExpr implements ExponentExpr {
    protected ExponentExpr lhs, rhs;

    public ExponentSumExpr(ExponentExpr lhs, ExponentExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public BigInteger evaluate() {
        return lhs.evaluate().add(rhs.evaluate());
    }

    @Override
    public Zn.ZnElement evaluateZn(Zn zn) {
        return lhs.evaluateZn(zn).add(rhs.evaluateZn(zn));
    }
}
