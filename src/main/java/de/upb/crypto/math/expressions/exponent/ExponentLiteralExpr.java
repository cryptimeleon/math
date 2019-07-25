package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.structures.integers.IntegerElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Map;

public class ExponentLiteralExpr implements ExponentExpr {
    protected BigInteger exponent;

    public ExponentLiteralExpr(BigInteger exponent) {
        this.exponent = exponent;
    }

    @Override
    public BigInteger evaluate() {
        return exponent;
    }

    @Override
    public Zn.ZnElement evaluateZn(Zn zn) {
        return zn.valueOf(exponent);
    }

    @Override
    public ExponentLiteralExpr substitute(Map<String, ? extends Expression> substitutions) {
        return this;
    }
}
