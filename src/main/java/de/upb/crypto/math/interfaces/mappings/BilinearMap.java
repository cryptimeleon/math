package de.upb.crypto.math.interfaces.mappings;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.PairingExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.math.BigInteger;
import java.util.function.BiFunction;

/**
 * A map G1 x G2 -> GT (for groups G1, G2, GT) that is linear in both the first and the second component.
 */
public interface BilinearMap extends BiFunction<GroupElement, GroupElement, GroupElement>, StandaloneRepresentable {
    public Group getG1();

    public Group getG2();

    public Group getGT();

    /**
     * Computes apply(g1,g2)^exponent.
     * (Depending on the bilinear map and the involved groups, this may be more efficiently implemented than computing it directly.
     * For example, implementations should do exponentiation in the group with the cheapest operation)
     */
    public GroupElement apply(GroupElement g1, GroupElement g2, BigInteger exponent);

    @Override
    public default GroupElement apply(GroupElement g1, GroupElement g2) {
        return apply(g1, g2, BigInteger.ONE);
    }

    /**
     * Computes apply(g1,g2)^exponent.
     * (Depending on the bilinear map and the involved groups, this may be more efficiently implemented than computing it directly.
     * For example, implementations should do exponentiation in the group with the cheapest operation)
     */
    public default GroupElement apply(GroupElement g1, GroupElement g2, ZnElement exponent) {
        return apply(g1, g2, exponent.getInteger());
    }

    public default PairingExpr expr(GroupElementExpression g1elem, GroupElementExpression g2elem) {
        return new PairingExpr(this, g1elem, g2elem);
    }

    public default PairingExpr expr(GroupElement g1elem, GroupElement g2elem) {
        return expr(g1elem.expr(), g2elem.expr());
    }

    public default GroupElementExpression expr() {
        return getGT().expr();
    }

    /**
     * Returns true if e(g,h) == e(h,g) for all g,h.
     */
    public boolean isSymmetric();
}
