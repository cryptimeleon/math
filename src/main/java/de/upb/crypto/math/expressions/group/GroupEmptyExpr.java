package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents the neutral group element of the given group. Has utility over using a
 * {@see GroupConstantExpression} with a neutral element in the sense that the neutral element
 * is replaced on a group operation, leading to saving one potential group operation.
 */
public class GroupEmptyExpr extends GroupElementExpression {

    public GroupEmptyExpr(Group group) {
        super(group);
    }

    @Override
    public GroupElement evaluateNaive() {
        return this.group.getNeutralElement();
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
    public GroupEmptyExpr substitute(Function<String, Expression> substitutionMap) {
        return this;
    }

    @Override
    public GroupEmptyExpr substitute(Substitutions variableValues) {
        return this;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {}
}
