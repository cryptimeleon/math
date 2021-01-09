package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExponentInvExpr implements ExponentExpr {
    protected final ExponentExpr child;

    public ExponentInvExpr(ExponentExpr child) {
        this.child = child;
    }

    public ExponentExpr getChild() {
        return child;
    }

    @Override
    public BigInteger evaluate() {
        throw new IllegalArgumentException("Cannot invert element over Z. Use evaluate(Zn) instead.");
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return child.evaluate(zn).inv();
    }

    @Override
    public ExponentExpr substitute(Substitution substitutions) {
        return child.substitute(substitutions).invert();
    }

    @Override
    public ExponentSumExpr linearize() throws IllegalArgumentException {
        if (child.containsVariables())
            throw new IllegalArgumentException("Cannot linearize - inversion of variables isn't linear");

        return new ExponentSumExpr(this, new ExponentEmptyExpr());
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(child);
    }
}
