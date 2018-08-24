package de.upb.crypto.math.interfaces.mappings;

import de.upb.crypto.math.interfaces.structures.FutureGroupElement;
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

    /**
     * Computes the product as given by the expression ({@see PairingProductExpression}).
     * <p>
     * This is generally much more efficient than computing the expression naively.
     */
    public GroupElement evaluate(PairingProductExpression expr);

    /**
     * Computes the value of the {@link PairingProductExpression}
     * This will usually be more efficient than
     * naively computing that product.
     * <p>
     * The result is being processed on another thread.
     * The result is a {@link FutureGroupElement}. When calling
     * any operation on the {@link FutureGroupElement}, the caller thread
     * may be blocked until the value is ready.
     *
     * @param expression {@link PairingProductExpression} to evaluate
     */
    default FutureGroupElement evaluateConcurrent(PairingProductExpression expression) {
        return new FutureGroupElement(() -> evaluate(expression));
    }

    /**
     * Returns true if e(g,h) == e(h,g) for all g,h.
     */
    public boolean isSymmetric();

    /**
     * @return empty {@link PairingProductExpression} for this map
     */
    default PairingProductExpression pairingProductExpression() {
        return new PairingProductExpression(this);
    }
}
