package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExponentSumExpr implements ExponentExpr {
    protected final ExponentExpr lhs, rhs;

    public ExponentSumExpr(ExponentExpr lhs, ExponentExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public ExponentExpr getLhs() {
        return lhs;
    }

    public ExponentExpr getRhs() {
        return rhs;
    }

    @Override
    public BigInteger evaluate() {
        return lhs.evaluate().add(rhs.evaluate());
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return lhs.evaluate(zn).add(rhs.evaluate(zn));
    }

    @Override
    public ExponentExpr substitute(Substitution substitutions) {
        return lhs.substitute(substitutions).add(rhs.substitute(substitutions));
    }

    @Override
    public ExponentSumExpr linearize() throws IllegalArgumentException {
        ExponentSumExpr lhsLinearized = lhs.linearize();
        ExponentSumExpr rhsLinearized = rhs.linearize();

        return new ExponentSumExpr(lhsLinearized.getLhs().add(rhsLinearized.getLhs()), lhsLinearized.getRhs().add(rhsLinearized.getRhs()));
    }

}
