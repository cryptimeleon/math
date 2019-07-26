package de.upb.crypto.math.expressions.group;


import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import javax.annotation.Nonnull;
import java.util.Map;

public class GroupInvExpr extends GroupElementExpression {
    protected GroupElementExpression base;

    public GroupInvExpr(@Nonnull GroupElementExpression base) {
        super(base.getDefaultEvaluator());
        this.base = base;
    }

    @Override
    public GroupElement evaluateNaive() {
        return base.evaluateNaive().inv();
    }

    @Override
    public GroupInvExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new GroupInvExpr(base.substitute(substitutions));
    }

    public GroupElementExpression getBase() {
        return base;
    }
}
