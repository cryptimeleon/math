package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExponentMulExpr implements ExponentExpr {
    protected ExponentExpr lhs, rhs;

    public ExponentMulExpr(ExponentExpr lhs, ExponentExpr rhs) {
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
        return lhs.evaluate().multiply(rhs.evaluate());
    }

    @Override
    public Zn.ZnElement evaluate(Zn zn) {
        return lhs.evaluate(zn).mul(rhs.evaluate(zn));
    }

    @Override
    public ExponentMulExpr substitute(Function<String, Expression> substitutionMap) {
        return new ExponentMulExpr(lhs.substitute(substitutionMap), rhs.substitute(substitutionMap));
    }

    @Override
    public ExponentMulExpr substitute(Substitutions variableValues) {
        return new ExponentMulExpr(lhs.substitute(variableValues), rhs.substitute(variableValues));
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        lhs.treeWalk(visitor);
        rhs.treeWalk(visitor);
    }

    @Override
    public ExponentExpr precompute() {
        return new ExponentMulExpr(lhs.precompute(), rhs.precompute());
    }
}
