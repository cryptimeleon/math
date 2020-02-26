package de.upb.crypto.math.expressions.test.trs;

import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.evaluator.trs.group.*;
import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.exponent.ExponentMulExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.*;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;

import java.math.BigInteger;

public class RuleTests {

    @Test
    public void testPairingGtExpRuleSimple() {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setDebugMode(true);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup group = fac.createBilinearGroup();
        GroupPowExpr powExpr = new GroupPowExpr(
                new PairingExpr(group.getBilinearMap(),
                        new GroupElementConstantExpr(group.getG1().getUniformlyRandomNonNeutral()),
                        new GroupElementConstantExpr(group.getG2().getUniformlyRandomNonNeutral())
                ),
                new ExponentConstantExpr(BigInteger.valueOf(2))
        );
        ExprRule pairingRule = new PairingGtExpRule();
        assert pairingRule.isApplicable(powExpr);

        GroupElementExpression newExpr = (GroupElementExpression) pairingRule.apply(powExpr);
        assert newExpr instanceof PairingExpr;
        assert powExpr.evaluateNaive().equals(newExpr.evaluateNaive());
    }

    @Test
    public void testPairingGtExpRuleDontMoveVar() {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setDebugMode(true);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup group = fac.createBilinearGroup();
        GroupPowExpr powExpr = new GroupPowExpr(
                new PairingExpr(group.getBilinearMap(),
                        new GroupElementConstantExpr(group.getG1().getUniformlyRandomNonNeutral()),
                        new GroupElementConstantExpr(group.getG2().getUniformlyRandomNonNeutral())
                ),
                new ExponentVariableExpr("x")
        );
        ExprRule pairingRule = new PairingGtExpRule();
        assert !pairingRule.isApplicable(powExpr);
    }

    @Test
    public void testExpSwapRuleSimple() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        GroupPowExpr expr = new GroupPowExpr(
                new GroupPowExpr(
                        new GroupElementConstantExpr(unitGroup.getUniformlyRandomNonNeutral()),
                        new ExponentVariableExpr("x")),
                new ExponentConstantExpr(BigInteger.valueOf(2))
        );
        ExprRule expSwapRule = new ExpSwapRule();
        assert expSwapRule.isApplicable(expr);

        GroupElementExpression newExpr = (GroupElementExpression) expSwapRule.apply(expr);
        assert newExpr instanceof GroupPowExpr;
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluateNaive().equals(newExpr.substitute(valueBundle).evaluateNaive());
    }

    @Test
    public void testPowExpMulLeftRule() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        GroupPowExpr expr = new GroupPowExpr(
                new GroupElementConstantExpr(unitGroup.getUniformlyRandomNonNeutral()),
                new ExponentMulExpr(
                        new ExponentConstantExpr(BigInteger.valueOf(2)),
                        new ExponentVariableExpr("x")
                )
        );
        ExprRule powExpMulLeftRule = new PowExpMulLeftRule();
        assert powExpMulLeftRule.isApplicable(expr);

        GroupElementExpression newExpr = (GroupElementExpression) powExpMulLeftRule.apply(expr);
        assert newExpr instanceof GroupPowExpr;
        assert ((GroupPowExpr) newExpr).getBase() instanceof  GroupPowExpr;
        assert ((GroupPowExpr) ((GroupPowExpr) newExpr).getBase()).getExponent().evaluate()
                .equals(BigInteger.valueOf(2));
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluateNaive().equals(newExpr.substitute(valueBundle).evaluateNaive());
    }

    @Test
    public void testPowExpMulRightRule() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        GroupPowExpr expr = new GroupPowExpr(
                new GroupElementConstantExpr(unitGroup.getUniformlyRandomNonNeutral()),
                new ExponentMulExpr(
                        new ExponentVariableExpr("x"),
                        new ExponentConstantExpr(BigInteger.valueOf(2))
                )
        );
        ExprRule powExpMulRightRule = new PowExpMulRightRule();
        assert powExpMulRightRule.isApplicable(expr);

        GroupElementExpression newExpr = (GroupElementExpression) powExpMulRightRule.apply(expr);
        assert newExpr instanceof GroupPowExpr;
        assert ((GroupPowExpr) newExpr).getBase() instanceof GroupPowExpr;
        assert ((GroupPowExpr) ((GroupPowExpr) newExpr).getBase()).getExponent().evaluate()
                .equals(BigInteger.valueOf(2));
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluateNaive().equals(newExpr.substitute(valueBundle).evaluateNaive());
    }

    @Test
    public void testOpInPowRuleAllVars() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        // (g_1^x * g_2^y)^z
        GroupPowExpr expr = new GroupPowExpr(
                new GroupOpExpr(
                        new GroupPowExpr(
                                unitGroup.getUniformlyRandomNonNeutral().expr(),
                                new ExponentVariableExpr("x")
                        ),
                        new GroupPowExpr(
                                unitGroup.getUniformlyRandomNonNeutral().expr(),
                                new ExponentVariableExpr("y")
                        )
                ),
                new ExponentVariableExpr("z")
        );

        ExprRule opInPowRule = new OpInPowRule();
        assert opInPowRule.isApplicable(expr);

        // newExpr = (g_1^x)^z * (g_2^y)^z
        GroupElementExpression newExpr = (GroupElementExpression) opInPowRule.apply(expr);
        assert newExpr instanceof GroupOpExpr;
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", BigInteger.valueOf(1));
        valueBundle.put("y", BigInteger.valueOf(2));
        valueBundle.put("z", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluateNaive().equals(newExpr.substitute(valueBundle).evaluateNaive());
    }

    @Test
    public void testOpInPowRuleNoVarsInside() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        // (g_1 * g_2^2)^z
        GroupPowExpr expr = new GroupPowExpr(
                new GroupOpExpr(
                        new GroupPowExpr(
                                unitGroup.getUniformlyRandomNonNeutral().expr(),
                                new ExponentConstantExpr(BigInteger.valueOf(1))
                        ),
                        new GroupPowExpr(
                                unitGroup.getUniformlyRandomNonNeutral().expr(),
                                new ExponentConstantExpr(BigInteger.valueOf(2))
                        )
                ),
                new ExponentVariableExpr("z")
        );

        ExprRule opInPowRule = new OpInPowRule();
        assert !opInPowRule.isApplicable(expr);
    }

    @Test
    public void testMergeNestedVarExpRule() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        // (g^x)^y
        GroupPowExpr expr = new GroupPowExpr(
                new GroupPowExpr(
                        unitGroup.getUniformlyRandomNonNeutral().expr(),
                        new ExponentVariableExpr("x")
                ),
                new ExponentVariableExpr("y")
        );

        ExprRule mergeNestedVarExpRule = new MergeNestedVarExpRule();
        assert mergeNestedVarExpRule.isApplicable(expr);

        // newExpr = g^{x*y}
        GroupElementExpression newExpr = (GroupElementExpression) mergeNestedVarExpRule.apply(expr);
        assert newExpr instanceof GroupPowExpr;
        GroupPowExpr powExpr = (GroupPowExpr) newExpr;
        assert powExpr.getExponent() instanceof ExponentMulExpr;
        assert powExpr.getBase() instanceof GroupElementConstantExpr;
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", BigInteger.valueOf(1));
        valueBundle.put("y", BigInteger.valueOf(2));
        assert expr.substitute(valueBundle).evaluateNaive().equals(newExpr.substitute(valueBundle).evaluateNaive());
    }

    @Test
    public void testMergeNestedConstExpRule() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        // (g^2)^3
        GroupPowExpr expr = new GroupPowExpr(
                new GroupPowExpr(
                        unitGroup.getUniformlyRandomNonNeutral().expr(),
                        new ExponentConstantExpr(BigInteger.valueOf(2))
                ),
                new ExponentConstantExpr(BigInteger.valueOf(3))
        );

        ExprRule mergeNestedConstExpRule = new MergeNestedConstExpRule();
        assert mergeNestedConstExpRule.isApplicable(expr);

        // newExpr = g^{2*3}
        GroupElementExpression newExpr = (GroupElementExpression) mergeNestedConstExpRule.apply(expr);
        assert newExpr instanceof GroupPowExpr;
        GroupPowExpr powExpr = (GroupPowExpr) newExpr;
        assert powExpr.getExponent() instanceof ExponentMulExpr;
        assert powExpr.getBase() instanceof GroupElementConstantExpr;
        assert expr.evaluateNaive().equals(newExpr.evaluateNaive());
    }
}
