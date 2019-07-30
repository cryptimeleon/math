package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.util.Map;
import java.util.function.Consumer;

public class GroupPowExpr extends GroupElementExpression {
    protected GroupElementExpression base;
    protected ExponentExpr exponent;

    public GroupPowExpr(GroupElementExpression base, ExponentExpr exponent) {
        super(base.getDefaultEvaluator());
        this.base = base;
        this.exponent = exponent;
    }

    @Override
    public GroupElement evaluateNaive() {
        return base.evaluateNaive().pow(exponent.evaluate());
    }

    @Override
    public GroupPowExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new GroupPowExpr(base.substitute(substitutions), exponent.substitute(substitutions));
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        base.treeWalk(visitor);
        exponent.treeWalk(visitor);
    }
}
