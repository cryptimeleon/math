package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.ValueBundle;
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
    public Zn.ZnElement evaluate(Zn zn) {
        return lhs.evaluate(zn).add(rhs.evaluate(zn));
    }

    @Override
    public ExponentSumExpr substitute(Function<String, Expression> substitutionMap) {
        return new ExponentSumExpr(lhs.substitute(substitutionMap), rhs.substitute(substitutionMap));
    }

    @Override
    public ExponentSumExpr substitute(Substitutions variableValues) {
        return new ExponentSumExpr(lhs.substitute(variableValues), rhs.substitute(variableValues));
    }

    @Override
    public ExponentExpr precompute() {
        return new ExponentSumExpr(lhs.precompute(), rhs.precompute());
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        lhs.treeWalk(visitor);
        rhs.treeWalk(visitor);
    }
}
