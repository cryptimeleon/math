package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

public class ExponentVariableExpr extends VariableExpression implements ExponentExpr {
    public ExponentVariableExpr(String name) {
        super(name);
    }

    @Override
    public BigInteger evaluate() {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    public Zn.ZnElement evaluateZn(Zn zn) {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }
}
