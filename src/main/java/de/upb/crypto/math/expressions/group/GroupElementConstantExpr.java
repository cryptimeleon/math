package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link GroupElementExpression} representing a constant group element.
 */
public class GroupElementConstantExpr extends GroupElementExpression {
    /**
     * The constant value represented by this expression.
     */
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
    public GroupElement evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        return value;
    }

    @Override
    public GroupElementExpression substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return this;
    }

    @Override
    protected GroupOpExpr linearize(ExponentExpr exponent) {
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
