package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Consumer;

public class ExponentSumExpr implements ExponentExpr {
    protected ExponentExpr lhs, rhs;

    public ExponentSumExpr(ExponentExpr lhs, ExponentExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public BigInteger evaluate() {
        return lhs.evaluate().add(rhs.evaluate());
    }

    @Override
    public Zn.ZnElement evaluateZn(Zn zn) {
        return lhs.evaluateZn(zn).add(rhs.evaluateZn(zn));
    }

    @Override
    public ExponentSumExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new ExponentSumExpr(lhs.substitute(substitutions), rhs.substitute(substitutions));
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        lhs.treeWalk(visitor);
        rhs.treeWalk(visitor);
    }
}
