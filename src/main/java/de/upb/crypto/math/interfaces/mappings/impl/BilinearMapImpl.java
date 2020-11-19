package de.upb.crypto.math.interfaces.mappings.impl;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.PairingExpr;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.math.BigInteger;
import java.util.function.BiFunction;

/**
 * A map G1 x G2 -> GT (for groups G1, G2, GT) that is linear in both the first and the second component.
 * <p>
 * Usually wrapped by a {@link BilinearMap} for actual use.
 */
public interface BilinearMapImpl extends BiFunction<GroupElementImpl, GroupElementImpl, GroupElementImpl> {
    /**
     * Computes {@code apply(g1,g2)^exponent}.
     * <p>
     * Depending on the bilinear map and the involved groups, this may be more efficiently implemented than computing
     * it directly via {@code apply(g1,g2).pow(exponent)}.
     * For example, implementations should do exponentiation in the group with the cheapest group operation.
     */
    GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2, BigInteger exponent);

    @Override
    default GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2) {
        return apply(g1, g2, BigInteger.ONE);
    }

    /**
     * Returns true if {@code e(g,h).equals(e(h,g))} for all {@code g}, {@code h}.
     */
    boolean isSymmetric();
}
