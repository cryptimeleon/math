package de.upb.crypto.math.expressions.test;

import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.bool.GroupEqualityExpr;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.expressions.group.GroupVariableExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;

import java.math.BigInteger;

public class OptGroupElementExpressionEvaluatorPrecomputeTest {
    @Test
    public void testBoolAndExprPrecompute() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();
        GroupEqualityExpr expr = new GroupEqualityExpr(
                new GroupPowExpr(
                        new GroupVariableExpr("x"),
                        new ExponentVariableExpr("a")
                ),
                new GroupPowExpr(
                        unitGroup.getUniformlyRandomNonNeutral().expr(),
                        new ExponentVariableExpr("b")
                )
        );

        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        BooleanExpression newExpr = evaluator.precompute(expr);
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", unitGroup.getUniformlyRandomNonNeutral());
        valueBundle.put("a", BigInteger.valueOf(2));
        valueBundle.put("b", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluate() == newExpr.substitute(valueBundle).evaluate();
    }
}
