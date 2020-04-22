package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.structures.RingAdditiveGroup;
import de.upb.crypto.math.structures.zn.Zn;

import java.util.function.Consumer;

public class ZnExponentAsAdditiveGroupElemExpr extends GroupElementExpression {
    protected Zn zn;
    protected ExponentExpr expr;
    public ZnExponentAsAdditiveGroupElemExpr(Zn zn, ExponentExpr expr) {
        this.zn = zn;
        this.expr = expr;
    }

    @Override
    public RingAdditiveGroup.RingAdditiveGroupElement evaluateNaive() {
        return expr.evaluate(zn).toAdditiveGroupElement();
    }

    @Override
    public RingAdditiveGroup.RingAdditiveGroupElement evaluate() {
        return expr.evaluate(zn).toAdditiveGroupElement();
    }

    @Override
    public RingAdditiveGroup.RingAdditiveGroupElement evaluate(Substitutions variableValues) {
        return expr.evaluate(zn, variableValues).toAdditiveGroupElement();
    }

    @Override
    public ZnExponentAsAdditiveGroupElemExpr substitute(Substitutions variableValues) {
        return new ZnExponentAsAdditiveGroupElemExpr(zn, expr.substitute(variableValues));
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        expr.treeWalk(visitor);
    }
}
