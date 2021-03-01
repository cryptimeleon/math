package org.cryptimeleon.math.structures.groups.elliptic;

import org.cryptimeleon.math.structures.groups.GroupElementImpl;

import java.math.BigInteger;
import java.util.function.BiFunction;

/**
 * Interface for implementing of a bilinear map {@code e : G1 x G2 -> GT}.
 * <p>
 * Bilinearity means that the map {@code e} is linear in both the first and second component.
 * {@code G1}, {@code G2} and {@code GT} are groups.
 * <p>
 * Usually wrapped by a {@link BilinearMap} for actual use.
 */
public interface BilinearMapImpl extends BiFunction<GroupElementImpl, GroupElementImpl, GroupElementImpl> {
    /**
     * Computes \(e(g1,g2)^\text{exponent}\).
     * <p>
     * Depending on the bilinear map and the involved groups, this may be more efficiently implemented than computing
     * it directly via {@code apply(g1,g2).pow(exponent)}.
     * For example, implementations should do exponentiation in the group with the cheapest group operation.
     *
     * @param g1 left hand side argument for the pairing function
     * @param g2 right hand side argument for the pairing function
     * @param exponent the exponent to apply to the result of the pairing
     */
    GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2, BigInteger exponent);

    /**
     * Corresponds to {@code this.apply(g1, g2, 1)}.
     *
     * @param g1 left hand side argument for the pairing function
     * @param g2 right hand side argument for the pairing function
     */
    @Override
    default GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2) {
        return apply(g1, g2, BigInteger.ONE);
    }

    /**
     * Returns true if \(e(g,h) = e(h,g)\) for all g in G1, h in G2.
     */
    boolean isSymmetric();
}
