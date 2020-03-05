package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

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
    public Zn.ZnElement evaluate(Zn zn) {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    public ExponentExpr substitute(Function<String, Expression> substitutionMap) {
        Expression result = substitutionMap.apply(name);
        return result == null ? this : (ExponentExpr) result;
    }

    @Override
    public ExponentExpr substitute(ValueBundle variableValues) {
        BigInteger result = variableValues.getInteger(name);
        return result == null ? this : new ExponentConstantExpr(result);
    }

    @Override
    public ExponentExpr precompute() {
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
    }
}
