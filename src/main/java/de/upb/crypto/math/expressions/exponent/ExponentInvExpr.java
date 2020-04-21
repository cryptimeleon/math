package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.ValueBundle;
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
    public ExponentInvExpr substitute(Substitutions variableValues) {
        return new ExponentInvExpr(child.substitute(variableValues));
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        child.treeWalk(visitor);
    }

    @Override
    public ExponentExpr precompute() {
        return new ExponentInvExpr(child.precompute());
    }
}