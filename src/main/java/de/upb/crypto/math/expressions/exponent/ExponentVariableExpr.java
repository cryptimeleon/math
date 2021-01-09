package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.*;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

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
