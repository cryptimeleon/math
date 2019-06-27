package de.upb.crypto.math.expressions.group;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.GroupElement;

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
}
