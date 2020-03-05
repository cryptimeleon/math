package de.upb.crypto.math.expressions.group;


import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

public class GroupInvExpr extends GroupElementExpression {
    protected GroupElementExpression base;

    public GroupInvExpr(@Nonnull GroupElementExpression base) {
        super(base.getGroup());
        this.base = base;
    }

    @Override
    public GroupElement evaluateNaive() {
        return base.evaluateNaive().inv();
    }

    @Override
    public GroupInvExpr substitute(Function<String, Expression> substitutionMap) {
        return new GroupInvExpr(base.substitute(substitutionMap));
    }

    @Override
    public GroupInvExpr substitute(ValueBundle variableValues) {
        return new GroupInvExpr(base.substitute(variableValues));
    }

    public GroupElementExpression getBase() {
        return base;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        visitor.accept(base);
    }

    @Override
    public GroupElementExpression inv() { //avoid double-inversion
        return base;
    }
}
