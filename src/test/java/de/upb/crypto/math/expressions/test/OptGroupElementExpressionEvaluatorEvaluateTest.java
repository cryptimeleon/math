package de.upb.crypto.math.expressions.test;

import de.upb.crypto.math.expressions.EvaluationException;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupVariableExpr;
import de.upb.crypto.math.expressions.group.PairingExpr;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.performance.expressions.ExpressionGenerator;
import org.graalvm.compiler.nodes.calc.IntegerDivRemNode;
import org.junit.Test;

public class OptGroupElementExpressionEvaluatorEvaluateTest {

    @Test(expected = EvaluationException.class)
    public void testVarExpr() {
        GroupVariableExpr expr = new GroupVariableExpr("x");
        expr.evaluate();
    }

    @Test
    public void testMultithreadedPairingEval() {
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setDebugMode(true);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup bilGroup = fac.createBilinearGroup();

        GroupElementExpression expr = new PairingExpr(
                bilGroup.getBilinearMap(),
                bilGroup.getG1().getNeutralElement().expr(),
                bilGroup.getG2().getNeutralElement().expr()
        );

        OptGroupElementExpressionEvaluator evaluator = new OptGroupElementExpressionEvaluator();
        evaluator.getConfig().setEnableMultithreadedPairingEvaluation(true);
        assert expr.evaluate(evaluator).equals(expr.evaluateNaive());
    }
}
