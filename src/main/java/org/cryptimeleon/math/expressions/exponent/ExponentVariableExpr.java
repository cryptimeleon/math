package org.cryptimeleon.math.expressions.exponent;

import org.cryptimeleon.math.expressions.EvaluationException;
import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.expressions.VariableExpression;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * An {@link ExponentExpr} representing a named variable.
 */
public interface ExponentVariableExpr extends ExponentExpr, VariableExpression {
    @Override
    default BigInteger evaluate() {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    default void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }

    @Override
    default Zn.ZnElement evaluate(Zn zn) {
        throw new EvaluationException(this, "Variable cannot be evaluated");
    }

    @Override
    default BigInteger evaluate(Substitution substitutions) {
        ExponentExpr substitution = (ExponentExpr) substitutions.getSubstitution(this);
        if (substitution == null)
            throw new EvaluationException(this, "Variable cannot be evaluated");
        return substitution.evaluate();
    }

    @Override
    default Zn.ZnElement evaluate(Zn zn, Substitution substitutions) {
        ExponentExpr substitution = (ExponentExpr) substitutions.getSubstitution(this);
        if (substitution == null)
            throw new EvaluationException(this, "Variable cannot be evaluated");
        return substitution.evaluate(zn);
    }

    @Override
    default ExponentExpr substitute(Substitution substitutions) {
        Expression replacement = substitutions.getSubstitution(this);
        if (replacement != null)
            return (ExponentExpr) replacement;
        return this;
    }

    @Override
    default ExponentSumExpr linearize() throws IllegalArgumentException {
        return new ExponentSumExpr(new ExponentEmptyExpr(), this);
    }
}
