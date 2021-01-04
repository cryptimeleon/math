package de.upb.crypto.math.expressions.group;


import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link GroupElementExpression} representing the inversion of another group element expression.
 */
public class GroupInvExpr extends GroupElementExpression {
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
    public GroupElement evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        return base.evaluate(substitutions).inv();
    }

    @Override
    public GroupElementExpression substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return base.substitute(substitutions).inv();
    }

    @Override
    public GroupElementExpression inv() { //avoid double-inversion
        return base;
    }

    @Override
    protected GroupOpExpr linearize(ExponentExpr exponent) {
        return base.linearize(exponent.negate());
    }
}
