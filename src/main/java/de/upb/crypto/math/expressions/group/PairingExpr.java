package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.expressions.VariableExpression;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.BilinearMap;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link GroupElementExpression} representing a bilinear pairing applied to two group element expressions.
 */
public class PairingExpr extends GroupElementExpression {
    protected GroupElementExpression lhs, rhs;
    protected BilinearMap map;

    public PairingExpr(BilinearMap map, GroupElementExpression lhs, GroupElementExpression rhs) {
        super(map.getGT());
        this.map = map;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /**
     * Returns the bilinear map that is used for this pairing expression.
     */
    public BilinearMap getMap() {
        return map;
    }

    /**
     * Retrieves the left argument of the pairing.
     */
    public GroupElementExpression getLhs() {
        return lhs;
    }

    /**
     * Retrieves the right argument of the pairing.
     */
    public GroupElementExpression getRhs() {
        return rhs;
    }

    @Override
    public void forEachChild(Consumer<Expression> action) {
        action.accept(lhs);
        action.accept(rhs);
    }

    @Override
    public GroupElement evaluate(Function<VariableExpression, ? extends Expression> substitutions) {
        return map.apply(lhs.evaluate(substitutions), rhs.evaluate(substitutions));
    }

    @Override
    public PairingExpr substitute(Function<VariableExpression, ? extends Expression> substitutions) {
        return new PairingExpr(map, lhs.substitute(substitutions), rhs.substitute(substitutions));
    }

    @Override
    protected GroupOpExpr linearize(ExponentExpr exponent) {
        if (exponent.containsVariables() || lhs.containsVariables() || rhs.containsVariables()) {
            return new GroupOpExpr(new GroupEmptyExpr(map.getGT()), new PairingExpr(map, lhs.linearize(exponent), rhs.linearize()).pow(exponent));
        }
        else {
            BigInteger groupSize = getGroupOrderIfKnown();
            BigInteger exponentVal = groupSize == null ? exponent.evaluate() : exponent.evaluate(new Zn(groupSize)).getInteger();
            return new GroupOpExpr(evaluate().pow(exponentVal).expr(), new GroupEmptyExpr(map.getGT()));
        }
    }
}
