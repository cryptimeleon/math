package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.ValueBundle;

import java.util.function.Consumer;
import java.util.function.Function;

public class BoolOrExpr implements BooleanExpression {
    BooleanExpression lhs, rhs;

    public BoolOrExpr(BooleanExpression lhs, BooleanExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean evaluate() {
        return lhs.evaluate() || rhs.evaluate();
    }

    @Override
    public BooleanExpression substitute(Function<String, Expression> substitutionMap) {
        return new BoolOrExpr(lhs.substitute(substitutionMap), rhs.substitute(substitutionMap));
    }

    @Override
    public BooleanExpression substitute(Substitutions variableValues) {
        return new BoolOrExpr(lhs.substitute(variableValues), rhs.substitute(variableValues));
    }

    @Override
    public BooleanExpression precompute() {
        return new BoolOrExpr(lhs.precompute(), rhs.precompute());
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        lhs.treeWalk(visitor);
        rhs.treeWalk(visitor);
    }

    public BooleanExpression getLhs() {
        return lhs;
    }

    public BooleanExpression getRhs() {
        return rhs;
    }
}
