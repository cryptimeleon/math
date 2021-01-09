package de.upb.crypto.math.expressions.group;


import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitution;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class GroupInvExpr extends AbstractGroupElementExpression {
    protected GroupElementExpression base;

    public GroupInvExpr(@Nonnull GroupElementExpression base) {
        super(base.getGroup());
        this.base = base;
    }

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
