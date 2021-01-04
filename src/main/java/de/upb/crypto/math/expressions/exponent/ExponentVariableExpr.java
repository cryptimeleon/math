package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An {@link ExponentExpr} representing a named variable.
 */
public class ExponentVariableExpr implements ExponentExpr, VariableExpression {

    /**
     * The name of this variable.
     */
    protected final String name;

    public ExponentVariableExpr(String name) {
        this.name = name;
    }

    @Override
    public BigInteger evaluate() {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    public BigInteger evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        ExponentExpr substitution = (ExponentExpr) substitutions.apply(this);
        if (substitution == null)
            throw new EvaluationException(this, "Variable cannot be evaluated");
        return substitution.evaluate();
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn, Function<VariableExpression, ? extends Expression> substitutions) {
        ExponentExpr substitution = (ExponentExpr) substitutions.apply(this);
        if (substitution == null)
            throw new EvaluationException(this, "Variable cannot be evaluated");
        return substitution.evaluate(zn);
    }

    @Override
    public ExponentExpr substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        Expression replacement = substitutions.apply(this);
        if (replacement != null)
            return (ExponentExpr) replacement;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }
}
