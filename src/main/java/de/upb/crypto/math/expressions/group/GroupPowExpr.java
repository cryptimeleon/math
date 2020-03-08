package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class GroupPowExpr extends GroupElementExpression {
    protected GroupElementExpression base;
    protected ExponentExpr exponent;

    public GroupPowExpr(GroupElementExpression base, ExponentExpr exponent) {
        super(base.getGroup());
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public GroupElement evaluateNaive() {
        BigInteger groupOrder = getGroup().size();
        if (groupOrder == null)
            return base.evaluateNaive().pow(exponent.evaluate());
        else
            return base.evaluateNaive().pow(exponent.evaluate(getGroup().getZn()));
    }

    @Override
    public GroupPowExpr substitute(Function<String, Expression> substitutionMap) {
        return new GroupPowExpr(base.substitute(substitutionMap), exponent.substitute(substitutionMap));
    }

    @Override
    public GroupPowExpr substitute(Substitutions variableValues) {
        return new GroupPowExpr(base.substitute(variableValues), exponent.substitute(variableValues));
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        base.treeWalk(visitor);
        exponent.treeWalk(visitor);
    }

    public GroupElementExpression getBase() {
        return base;
    }

    public ExponentExpr getExponent() {
        return exponent;
    }

    @Override
    public GroupElementExpression pow(ExponentExpr exponent) {
        return new GroupPowExpr(base, this.exponent.mul(exponent));
    }
}
