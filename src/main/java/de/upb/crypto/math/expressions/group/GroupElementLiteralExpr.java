package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import javax.annotation.Nonnull;
import java.util.Map;

public class GroupElementLiteralExpr extends GroupElementExpression {
    protected GroupElement value;

    public GroupElementLiteralExpr(@Nonnull GroupElement value) {
        super(value.getStructure().getExpressionEvaluator());
        this.value = value;
    }

    @Override
    public GroupElement evaluateNaive() {
        return value;
    }

    @Override
    public GroupElementLiteralExpr substitute(Map<String, ? extends Expression> substitutions) {
        return this;
    }
}
