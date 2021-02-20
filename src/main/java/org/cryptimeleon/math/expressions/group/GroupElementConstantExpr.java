package org.cryptimeleon.math.expressions.group;

import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.expressions.exponent.ExponentExpr;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.function.Consumer;
/**
 * A {@link GroupElementExpression} representing a constant group element.
 */
public class GroupElementConstantExpr extends AbstractGroupElementExpression {
    protected final GroupElement value;

    public GroupElementConstantExpr(@Nonnull GroupElement value) {
        super(value.getStructure());
        this.value = value;
    }

    @Override
    public GroupElement evaluate() {
        return value;
    }

    @Override
    public GroupElement evaluate(Substitution substitutions) {
        return value;
    }

    @Override
    public GroupElementExpression substitute(Substitution substitutions) {
        return this;
    }

    @Override
    public GroupOpExpr linearize() throws IllegalArgumentException {
        return new GroupOpExpr(this, new GroupEmptyExpr(value.getStructure()));
    }

    @Override
    public GroupOpExpr flatten(ExponentExpr exponent) {
        if (exponent.containsVariables()) {
            return new GroupOpExpr(new GroupEmptyExpr(getGroup()), this.pow(exponent));
        } else {
            BigInteger groupSize = getGroupOrderIfKnown();
            if (groupSize == null)
                return new GroupOpExpr(value.pow(exponent.evaluate()).expr(), new GroupEmptyExpr(getGroup()));
            else
                return new GroupOpExpr(value.pow(exponent.evaluate(new Zn(groupSize))).expr(), new GroupEmptyExpr(getGroup()));
        }
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        //Nothing to do
    }
}
