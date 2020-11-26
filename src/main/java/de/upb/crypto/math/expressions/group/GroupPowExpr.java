package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link GroupElementExpression} representing a a group element expression raised to some power.
 */
public class GroupPowExpr extends GroupElementExpression {
    protected GroupElementExpression base;
    protected ExponentExpr exponent;

    public GroupPowExpr(GroupElementExpression base, ExponentExpr exponent) {
        super(base.getGroup());
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public GroupElement evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        BigInteger groupOrder = getGroupOrderIfKnown();

        if (groupOrder == null)
            return base.evaluate(substitutions).pow(exponent.evaluate(substitutions));
        else
            return base.evaluate(substitutions).pow(exponent.evaluate(getGroup().getZn(), substitutions));
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(base);
        action.accept(exponent);
    }

    @Override
    public GroupElementExpression substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return base.substitute(substitutions).pow(exponent.substitute(substitutions));
    }

    /**
     * Retrieves the base expression of this exponentiation.
     */
    public GroupElementExpression getBase() {
        return base;
    }

    /**
     * Retrieves the exponent expression of this exponentiation.
     */
    public ExponentExpr getExponent() {
        return exponent;
    }

    @Override
    public GroupElementExpression pow(ExponentExpr exp) {
        return new GroupPowExpr(base, this.exponent.mul(exp));
    }

    @Override
    protected GroupOpExpr linearize(ExponentExpr exp) {
        return base.linearize(exp.mul(this.exponent));
    }
}
