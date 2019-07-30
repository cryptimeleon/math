package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Consumer;

public class GroupEmptyExpr extends GroupElementExpression {

    public GroupEmptyExpr(Group group) {
        super(group.getExpressionEvaluator());
    }

    public GroupEmptyExpr(GroupElementExpressionEvaluator evaluator) {
        super(evaluator);
    }

    @Override
    public GroupElement evaluateNaive() {
        throw new EvaluationException(this, "Cannot evaluate empty expression");
    }

    @Override
    public GroupElementExpression op(GroupElementExpression rhs) {
        return rhs;
    }

    @Override
    public GroupElementExpression op(GroupElement rhs) {
        return op(new GroupElementLiteralExpr(rhs));
    }

    @Override
    public GroupElementExpression pow(ExponentExpr exponent) {
        return this;
    }

    @Override
    public GroupElementExpression pow(BigInteger exponent) {
        return this;
    }

    @Override
    public GroupElementExpression pow(Zn.ZnElement exponent) {
        return this;
    }

    @Override
    public GroupElementExpression inv() {
        return this;
    }

    @Override
    public GroupEmptyExpr substitute(Map<String, ? extends Expression> substitutions) {
        return this;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {}
}
