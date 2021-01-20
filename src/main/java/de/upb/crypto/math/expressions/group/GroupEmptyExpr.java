package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.structures.groups.Group;
import de.upb.crypto.math.structures.groups.GroupElement;
import de.upb.crypto.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * Represents the neutral group element of the given group. Has utility over using a
 * {@link GroupElementConstantExpr} with a neutral element in the sense that the neutral element
 * is replaced on a group operation, leading to saving one potential group operation.
 */
public class GroupEmptyExpr extends AbstractGroupElementExpression {

    public GroupEmptyExpr(Group group) {
        super(group);
    }

    public GroupEmptyExpr() {
        super();
    }

    @Override
    public GroupElement evaluate(Substitution substitutions) {
        return this.group.getNeutralElement();
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }

    @Override
    public GroupElementExpression substitute(Substitution substitutions) {
        return this;
    }

    @Override
    public GroupElementExpression op(GroupElementExpression rhs) {
        return rhs;
    }

    @Override
    public GroupElementExpression op(GroupElement rhs) {
        return op(new GroupElementConstantExpr(rhs));
    }

    @Override
    public GroupElementExpression pow(ExponentExpr exponent) {
        return this;
    }

    @Override
    public GroupElementExpression pow(BigInteger exponent) {
        return this;
    }

    @Override
    public GroupElementExpression pow(Zn.ZnElement exponent) {
        return this;
    }

    @Override
    public GroupElementExpression inv() {
        return this;
    }

    @Override
    public GroupOpExpr linearize() throws IllegalArgumentException {
        return new GroupOpExpr(this, this);
    }

    @Override
    public GroupOpExpr flatten(ExponentExpr exponent) {
        return new GroupOpExpr(this, this);
    }
}
