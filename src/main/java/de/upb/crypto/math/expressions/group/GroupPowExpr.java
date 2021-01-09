package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.exponent.ExponentSumExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.function.Consumer;

public class GroupPowExpr extends AbstractGroupElementExpression {
    protected GroupElementExpression base;
    protected ExponentExpr exponent;

    public GroupPowExpr(GroupElementExpression base, ExponentExpr exponent) {
        super(base.getGroup());
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public GroupElement evaluate(Substitution substitutions) {
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
    public GroupElementExpression substitute(Substitution substitutions) {
        return base.substitute(substitutions).pow(exponent.substitute(substitutions));
    }

    public GroupElementExpression getBase() {
        return base;
    }

    public ExponentExpr getExponent() {
        return exponent;
    }

    @Override
    public GroupElementExpression pow(ExponentExpr exp) {
        return new GroupPowExpr(base, this.exponent.mul(exp));
    }

    @Override
    public GroupOpExpr linearize() throws IllegalArgumentException {
        boolean baseHasVariables = base.containsVariables();
        boolean exponentHasVariables = exponent.containsVariables();

        if (baseHasVariables && exponentHasVariables)
            throw new IllegalArgumentException("Cannot linearize this expression (it's of the form g^x, where both g and x depend on variables)");

        if (!baseHasVariables && !exponentHasVariables)
            return new GroupOpExpr(this, new GroupEmptyExpr(base.getGroup()));

        if (baseHasVariables) { //hence exponent doesn't
            GroupOpExpr baseLinear = base.linearize();
            return new GroupOpExpr(baseLinear.getLhs().pow(exponent), baseLinear.getRhs().pow(exponent));
        } else { //exponent has variables, base doesn't.
            ExponentSumExpr exponentLinear = exponent.linearize();
            return new GroupOpExpr(base.pow(exponentLinear.getLhs()), base.pow(exponentLinear.getRhs()));
        }
    }

    @Override
    public GroupOpExpr flatten(ExponentExpr exp) {
        return base.flatten(exp.mul(this.exponent));
    }
}
