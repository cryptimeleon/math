package de.upb.crypto.math.expressions.exponent;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.Substitutions;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.structures.RingAdditiveGroup;
import de.upb.crypto.math.interfaces.structures.RingUnitGroup;
import de.upb.crypto.math.structures.zn.Zn;

import java.util.function.Consumer;

public class ZnExponentAsMultiplicativeGroupElemExpr extends GroupElementExpression {
    protected Zn zn;
    protected ExponentExpr expr;
    public ZnExponentAsMultiplicativeGroupElemExpr(Zn zn, ExponentExpr expr) {
        super(zn.asUnitGroup());
        this.zn = zn;
        this.expr = expr;
    }

    @Override
    public RingUnitGroup.RingUnitGroupElement evaluateNaive() {
        return expr.evaluate(zn).toUnitGroupElement();
    }

    @Override
    public RingUnitGroup.RingUnitGroupElement evaluate() {
        return expr.evaluate(zn).toUnitGroupElement();
    }

    @Override
    public RingUnitGroup.RingUnitGroupElement evaluate(Substitutions variableValues) {
        return expr.evaluate(zn, variableValues).toUnitGroupElement();
    }

    @Override
    public ZnExponentAsMultiplicativeGroupElemExpr substitute(Substitutions variableValues) {
        return new ZnExponentAsMultiplicativeGroupElemExpr(zn, expr.substitute(variableValues));
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        expr.treeWalk(visitor);
    }
}
