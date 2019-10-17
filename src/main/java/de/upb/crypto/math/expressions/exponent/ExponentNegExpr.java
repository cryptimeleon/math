package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExponentNegExpr implements ExponentExpr {
    protected ExponentExpr child;

    public ExponentNegExpr(ExponentExpr child) {
        this.child = child;
    }

    public ExponentExpr getChild() {
        return child;
    }

    @Override
    public BigInteger evaluate() {
        return child.evaluate().negate();
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return child.evaluate(zn).neg();
    }

    @Override
    public ExponentExpr substitute(Function<String, Expression> substitutionMap) {
        return new ExponentNegExpr(child.substitute(substitutionMap));
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        child.treeWalk(visitor);
    }

    @Override
    public ExponentExpr precompute() {
        return new ExponentNegExpr(child.precompute());
    }
}