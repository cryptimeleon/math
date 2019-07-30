package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.util.Map;
import java.util.function.Consumer;

public class PairingExpr extends GroupElementExpression {
    protected GroupElementExpression lhs, rhs;
    protected BilinearMap map;

    public PairingExpr(BilinearMap map, GroupElementExpression lhs, GroupElementExpression rhs) {
        super(lhs.getDefaultEvaluator());
        this.map = map;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public GroupElement evaluateNaive() {
        return map.apply(this.lhs.evaluateNaive(), this.rhs.evaluateNaive());
    }

    @Override
    public PairingExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new PairingExpr(map, lhs.substitute(substitutions), rhs.substitute(substitutions));
    }

    public BilinearMap getMap() {
        return map;
    }

    public GroupElementExpression getLhs() {
        return lhs;
    }

    public GroupElementExpression getRhs() {
        return rhs;
    }

    @Override
    public void treeWalk(Consumer<Expression> visitor) {
        visitor.accept(this);
        lhs.treeWalk(visitor);
        rhs.treeWalk(visitor);
    }
}
