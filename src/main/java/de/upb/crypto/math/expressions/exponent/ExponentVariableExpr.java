package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Map;

public class ExponentVariableExpr implements ExponentExpr, VariableExpression {
    protected final String name;
    public ExponentVariableExpr(String name) {
        this.name = name;
    }

    @Override
    public BigInteger evaluate() {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    public Zn.ZnElement evaluateZn(Zn zn) {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    public ExponentExpr substitute(Map<String, ? extends Expression> substitutions) {
        if (substitutions.containsKey(name))
            return (ExponentExpr) substitutions.get(name);
        else
            return this;
    }

    @Override
    public String getName() {
        return name;
    }
}
