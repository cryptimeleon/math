package org.cryptimeleon.math.expressions.group;


import org.cryptimeleon.math.expressions.Expression;
import org.cryptimeleon.math.expressions.Substitution;
import org.cryptimeleon.math.expressions.exponent.ExponentExpr;
import org.cryptimeleon.math.structures.groups.GroupElement;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
/**
 * A {@link GroupElementExpression} representing the inversion of another group element expression.
 */
public class GroupInvExpr extends AbstractGroupElementExpression {
    protected GroupElementExpression base;

    public GroupInvExpr(@Nonnull GroupElementExpression base) {
        super(base.getGroup());
        this.base = base;
    }

    /**
     * Retrieves the group element expression being inverted.
     */
    public GroupElementExpression getBase() {
        return base;
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(base);
    }

    @Override
    public GroupElement evaluate(Substitution substitutions) {
        return base.evaluate(substitutions).inv();
    }

    @Override
    public GroupElementExpression substitute(Substitution substitutions) {
        return base.substitute(substitutions).inv();
    }

    @Override
    public GroupElementExpression inv() { //avoid double-inversion
        return base;
    }

    @Override
    public GroupOpExpr linearize() throws IllegalArgumentException {
        GroupOpExpr baseLinear = base.linearize();
        return new GroupOpExpr(baseLinear.getLhs().inv(), baseLinear.getRhs().inv());
    }

    @Override
    public GroupOpExpr flatten(ExponentExpr exponent) {
        return base.flatten(exponent.negate());
    }
}
