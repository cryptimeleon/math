package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class GroupElementConstantExpr extends GroupElementExpression {
    protected final GroupElement value;

    public GroupElementConstantExpr(@Nonnull GroupElement value) {
        super(value.getStructure());
        this.value = value;
    }

    @Override
    public GroupElement evaluateNaive() {
        return value;
    }

    @Override
    public GroupElementConstantExpr substitute(Function<String, Expression> substitutionMap) {
        return this;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
    }
}
