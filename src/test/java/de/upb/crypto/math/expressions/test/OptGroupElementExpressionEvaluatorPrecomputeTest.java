package de.upb.crypto.math.expressions.test;

import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.bool.*;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.exponent.ExponentEmptyExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.GroupElementConstantExpr;
import de.upb.crypto.math.expressions.group.GroupEmptyExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.expressions.group.GroupVariableExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;

import java.math.BigInteger;

public class OptGroupElementExpressionEvaluatorPrecomputeTest {
    @Test
    public void testSingleGroupEqualityExprPrecompute() {
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
        assert newExpr instanceof GroupEqualityExpr;
        assert ((GroupEqualityExpr) newExpr).getRhs() instanceof GroupElementConstantExpr;
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", unitGroup.getUniformlyRandomNonNeutral());
        valueBundle.put("a", BigInteger.valueOf(2));
        valueBundle.put("b", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluate() == newExpr.substitute(valueBundle).evaluate();
    }

    @Test
    public void testPrecomputeAllBooleanTypes() {
        Zp zp = new Zp(new BigInteger("170141183460469231731687303715884105727"));
        Group unitGroup = zp.asUnitGroup();

        BooleanExpression expr = new BoolOrExpr(
                new BoolOrExpr(
                        new BoolNotExpr(
                                new ExponentEqualityExpr(
                                        new ExponentConstantExpr(BigInteger.valueOf(2)),
                                        new ExponentEmptyExpr()
                                )
                        ),
                        new BoolOrExpr(
                                new BoolVariableExpr("x"),
                                new BoolEmptyExpr()
                        )
                ),
                new BoolAndExpr(
                        new GroupEqualityExpr(
                                unitGroup.getUniformlyRandomNonNeutral().expr(),
                                unitGroup.getUniformlyRandomNonNeutral().expr()
                        ),
                        new GroupEqualityExpr(
                                new GroupVariableExpr("y"),
                                new GroupEmptyExpr(unitGroup)
                        )
                )
        );

        BooleanExpression newExpr = new OptGroupElementExpressionEvaluator().precompute(expr);
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", true);
        valueBundle.put("y", unitGroup.getNeutralElement());
        assert expr.substitute(valueBundle).evaluate() == newExpr.substitute(valueBundle).evaluate();
    }

    @Test
    public void testMergeableExprPrecompute() {
        Zp zp = new Zp(new BigInteger("170141183460469231731687303715884105727"));
        Group unitGroup = zp.asUnitGroup();

        BoolAndExpr expr = new BoolAndExpr(
                new BoolNotExpr(new GroupEqualityExpr(
                        unitGroup.getUniformlyRandomNonNeutral().expr(), unitGroup.getUniformlyRandomNonNeutral().expr()
                )),
                new BoolAndExpr(
                        new GroupEqualityExpr(
                                unitGroup.getUniformlyRandomNonNeutral().expr(), new GroupEmptyExpr(unitGroup)
                        ),
                        new GroupEqualityExpr(
                                unitGroup.getUniformlyRandomNonNeutral().expr(), new GroupVariableExpr("y")
                        )
                )
        );
        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setEnablePrecomputeProbabilisticANDMerging(true);
        BooleanExpression newExpr = evaluator.precompute(expr);
        assert newExpr instanceof BoolAndExpr;
        BoolAndExpr andExpr = (BoolAndExpr) newExpr;
        assert andExpr.getRhs() instanceof GroupEqualityExpr;
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("y", unitGroup.getNeutralElement());
        assert expr.substitute(valueBundle).evaluate() == newExpr.substitute(valueBundle).evaluate();
    }
}
