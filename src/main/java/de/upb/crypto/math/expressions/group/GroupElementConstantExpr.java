package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import javax.annotation.Nonnull;
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
    public GroupElementConstantExpr substitute(Substitutions variableValues) {
        return this;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
    }
}
