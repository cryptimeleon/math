package de.upb.crypto.math.expressions.bool;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;

import java.util.function.Consumer;

public class BoolOrExpr implements BooleanExpression {
    BooleanExpression lhs, rhs;

    public BoolOrExpr(BooleanExpression lhs, BooleanExpression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public BooleanExpression getLhs() {
        return lhs;
    }

    public BooleanExpression getRhs() {
        return rhs;
    }

    @Override
    public BooleanExpression substitute(Substitution substitutions) {
        return lhs.substitute(substitutions).or(rhs.substitute(substitutions));
    }

    @Override
    public Boolean evaluate(Substitution substitutions) {
        return lhs.evaluate(substitutions) || rhs.evaluate(substitutions);
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }
}
