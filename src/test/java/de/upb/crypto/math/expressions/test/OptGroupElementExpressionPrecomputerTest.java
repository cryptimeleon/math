package de.upb.crypto.math.expressions.test;

import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.evaluator.ExponentExpressionAnalyzer;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionPrecomputer;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.evaluator.trs.group.ExpSwapRule;
import de.upb.crypto.math.expressions.evaluator.trs.group.PairingGtExpRule;
import de.upb.crypto.math.expressions.evaluator.trs.RuleApplicator;
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
import java.util.LinkedList;
import java.util.List;

public class OptGroupElementExpressionPrecomputerTest {

    @Test
    public void testRewriteTermsSimpleExpSwapRule() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        GroupPowExpr expr = new GroupPowExpr(
                new GroupPowExpr(
                        new GroupElementConstantExpr(unitGroup.getUniformlyRandomNonNeutral()),
                        new ExponentVariableExpr("x")),
                new ExponentConstantExpr(BigInteger.valueOf(2))
        );

        List<ExprRule> rules = new LinkedList<>();
        rules.add(new ExpSwapRule());
        rules.add(new PairingGtExpRule());
        GroupElementExpression newExpr = new OptGroupElementExpressionPrecomputer()
                .rewriteTermsTopDown(expr, new RuleApplicator(rules));
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluateNaive().equals(newExpr.substitute(valueBundle).evaluateNaive());
    }

    @Test
    public void testRewriteTermsPairingExpSwap() {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setDebugMode(true);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup group = fac.createBilinearGroup();
        GroupPowExpr expr = new GroupPowExpr(
                new GroupPowExpr(
                        new PairingExpr(
                                group.getBilinearMap(),
                                new GroupElementConstantExpr(group.getG1().getUniformlyRandomNonNeutral()),
                                new GroupElementConstantExpr(group.getG2().getUniformlyRandomNonNeutral())
                        ),
                        new ExponentVariableExpr("x")
                ),
                new ExponentConstantExpr(BigInteger.valueOf(2))
        );

        List<ExprRule> rules = new LinkedList<>();
        rules.add(new ExpSwapRule());
        rules.add(new PairingGtExpRule());
        GroupElementExpression newExpr = new OptGroupElementExpressionPrecomputer()
                .rewriteTerms(expr, new RuleApplicator(rules));
        assert newExpr instanceof GroupPowExpr;
        GroupPowExpr powExpr = (GroupPowExpr) newExpr;
        assert powExpr.getExponent().getVariables().contains("x");
        assert powExpr.getBase() instanceof  PairingExpr;
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluateNaive().equals(newExpr.substitute(valueBundle).evaluateNaive());
    }

    @Test
    public void testRewriteTermsExpOptimization() {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        // (g^{2*x})^{y*3}
        GroupElementExpression expr = new GroupPowExpr(
                new GroupPowExpr(
                        unitGroup.getUniformlyRandomNonNeutral().expr(),
                        new ExponentMulExpr(
                                new ExponentConstantExpr(BigInteger.valueOf(2)),
                                new ExponentVariableExpr("x")
                        )
                ),
                new ExponentMulExpr(
                        new ExponentVariableExpr("y"),
                        new ExponentConstantExpr(BigInteger.valueOf(3))
                )
        );

        GroupElementExpression newExpr = new OptGroupElementExpressionPrecomputer().rewriteTerms(expr);
        assert newExpr instanceof GroupPowExpr;
        GroupPowExpr powExpr1 = (GroupPowExpr) newExpr;
        assert powExpr1.getBase() instanceof GroupPowExpr;
        assert !ExponentExpressionAnalyzer.containsVariableExpr(((GroupPowExpr) powExpr1.getBase()).getExponent());
        assert ExponentExpressionAnalyzer.containsVariableExpr(powExpr1.getExponent());
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("x", BigInteger.valueOf(3));
        valueBundle.put("y", BigInteger.valueOf(2));
        assert expr.substitute(valueBundle).evaluateNaive().equals(newExpr.substitute(valueBundle).evaluateNaive());
    }

    @Test
    public void testVariableInBase() {
        Zp zp = new Zp(BigInteger.valueOf(101));

        // x^e
        GroupElementExpression expr = new GroupPowExpr(
                new GroupVariableExpr("x"),
                new ExponentConstantExpr(zp.getUniformlyRandomElement())
        );
        GroupElementExpression newExpr = new OptGroupElementExpressionEvaluator().precompute(expr);
    }
}
