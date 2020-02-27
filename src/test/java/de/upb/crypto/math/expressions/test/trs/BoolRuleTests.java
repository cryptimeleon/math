package de.upb.crypto.math.expressions.test.trs;

import de.upb.crypto.math.expressions.ValueBundle;
import de.upb.crypto.math.expressions.bool.GroupEqualityExpr;
import de.upb.crypto.math.expressions.evaluator.trs.ExprRule;
import de.upb.crypto.math.expressions.evaluator.trs.bool.MoveEqTestToOneSideRule;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.GroupPowExpr;
import de.upb.crypto.math.expressions.group.GroupVariableExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;

import java.math.BigInteger;

public class BoolRuleTests {

    @Test
    public void testMoveEqTestToOneSideRuleNoConstants()  {
        GroupEqualityExpr expr = new GroupEqualityExpr(
                new GroupPowExpr(
                        new GroupVariableExpr("x"),
                        new ExponentVariableExpr("a")
                ),
                new GroupPowExpr(
                        new GroupVariableExpr("y"),
                        new ExponentVariableExpr("b")
                )
        );
        // No constants so not applicable
        ExprRule moveEqTestToOneSideRule = new MoveEqTestToOneSideRule();
        assert !moveEqTestToOneSideRule.isApplicable(expr);
    }

    @Test
    public void testMoveEqTestToOneSideRuleLeftConstant()  {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        GroupEqualityExpr expr = new GroupEqualityExpr(
                new GroupPowExpr(
                        unitGroup.getUniformlyRandomNonNeutral().expr(),
                        new ExponentVariableExpr("a")
                ),
                new GroupPowExpr(
                        new GroupVariableExpr("y"),
                        new ExponentVariableExpr("b")
                )
        );
        // No constants so not applicable
        ExprRule moveEqTestToOneSideRule = new MoveEqTestToOneSideRule();
        assert moveEqTestToOneSideRule.isApplicable(expr);

        GroupEqualityExpr newExpr = (GroupEqualityExpr) moveEqTestToOneSideRule.apply(expr);
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("a", BigInteger.valueOf(2));
        valueBundle.put("y", unitGroup.getUniformlyRandomNonNeutral());
        valueBundle.put("b", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluate() == newExpr.substitute(valueBundle).evaluate();
    }

    @Test
    public void testMoveEqTestToOneSideRuleRightConstant()  {
        Zp zp = new Zp(BigInteger.valueOf(101));
        Group unitGroup = zp.asUnitGroup();

        GroupEqualityExpr expr = new GroupEqualityExpr(
                new GroupPowExpr(
                        new GroupVariableExpr("y"),
                        new ExponentVariableExpr("b")
                ),
                new GroupPowExpr(
                        unitGroup.getUniformlyRandomNonNeutral().expr(),
                        new ExponentVariableExpr("a")
                )
        );
        // No constants so not applicable
        ExprRule moveEqTestToOneSideRule = new MoveEqTestToOneSideRule();
        assert moveEqTestToOneSideRule.isApplicable(expr);

        GroupEqualityExpr newExpr = (GroupEqualityExpr) moveEqTestToOneSideRule.apply(expr);
        ValueBundle valueBundle = new ValueBundle();
        valueBundle.put("a", BigInteger.valueOf(2));
        valueBundle.put("y", unitGroup.getUniformlyRandomNonNeutral());
        valueBundle.put("b", BigInteger.valueOf(3));
        assert expr.substitute(valueBundle).evaluate() == newExpr.substitute(valueBundle).evaluate();
    }
}
