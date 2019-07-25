package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Map;

public interface ExponentExpr extends Expression {
    BigInteger evaluate();
    Zn.ZnElement evaluateZn(Zn zn);

    @Override
    ExponentExpr substitute(Map<String, ? extends Expression> substitutions);
}
