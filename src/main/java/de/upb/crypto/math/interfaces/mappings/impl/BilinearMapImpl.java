package de.upb.crypto.math.interfaces.mappings.impl;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.PairingExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.math.BigInteger;
import java.util.function.BiFunction;

/**
 * A map G1 x G2 -> GT (for groups G1, G2, GT) that is linear in both the first and the second component.
 */
public interface BilinearMapImpl extends BiFunction<GroupElementImpl, GroupElementImpl, GroupElementImpl> {
    /**
     * Computes apply(g1,g2)^exponent.
     * (Depending on the bilinear map and the involved groups, this may be more efficiently implemented than computing it directly.
     * For example, implementations should do exponentiation in the group with the cheapest operation)
     */
    GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2, BigInteger exponent);

    @Override
    default GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2) {
        return apply(g1, g2, BigInteger.ONE);
    }

    /**
     * Returns true if e(g,h) == e(h,g) for all g,h.
     */
    boolean isSymmetric();
}
