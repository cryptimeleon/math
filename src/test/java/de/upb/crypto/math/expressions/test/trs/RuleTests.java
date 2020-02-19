package de.upb.crypto.math.expressions.test.trs;

import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.evaluator.trs.ExpSwapRule;
import de.upb.crypto.math.expressions.evaluator.trs.GroupExprRule;
import de.upb.crypto.math.expressions.evaluator.trs.PairingGtExpRule;
import de.upb.crypto.math.expressions.evaluator.trs.PowExpMulLeftRule;
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
        GroupExprRule pairingRule = new PairingGtExpRule();
        assert pairingRule.isApplicable(powExpr);

        GroupElementExpression newExpr = pairingRule.apply(powExpr);
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
        GroupExprRule pairingRule = new PairingGtExpRule();
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
        GroupExprRule expSwapRule = new ExpSwapRule();
        assert expSwapRule.isApplicable(expr);

        GroupElementExpression newExpr = expSwapRule.apply(expr);
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
        GroupExprRule powExpMulLeftRule = new PowExpMulLeftRule();
        assert powExpMulLeftRule.isApplicable(expr);

        GroupElementExpression newExpr = powExpMulLeftRule.apply(expr);
        assert newExpr instanceof GroupPowExpr;
        assert ((GroupPowExpr) newExpr).getBase() instanceof  GroupPowExpr;
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluateNaive().equals(newExpr.substitute(valueBundle).evaluateNaive());
    }
}
