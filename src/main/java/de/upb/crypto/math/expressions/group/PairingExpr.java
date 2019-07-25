package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.expressions.Expression;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.util.Map;

public class PairingExpr implements GroupElementExpression {
    protected BilinearMap map;
    protected GroupElementExpression lhs, rhs;

    public PairingExpr(BilinearMap map, GroupElementExpression lhs, GroupElementExpression rhs) {
        this.map = map;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public GroupElement evaluate() {
        return map.apply(this.lhs.evaluate(), this.rhs.evaluate());
    }

    @Override
    public PairingExpr substitute(Map<String, ? extends Expression> substitutions) {
        return new PairingExpr(map, lhs.substitute(substitutions), rhs.substitute(substitutions));
    }
}
