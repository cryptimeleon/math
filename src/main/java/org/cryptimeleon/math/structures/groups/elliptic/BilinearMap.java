package org.cryptimeleon.math.structures.groups.elliptic;

import org.cryptimeleon.math.expressions.group.GroupElementExpression;
import org.cryptimeleon.math.expressions.group.PairingExpr;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.function.BiFunction;

/**
 * A bilinear map {@code e : G1 x G2 -> GT}.
 * <p>
 * Bilinearity means that the map {@code e} is linear in both the first and second component.
 * {@code G1}, {@code G2} and {@code GT} are groups.
 * <p>
 * Usually used as a wrapper around a {@link BilinearMapImpl} to offer additional evaluation capabilities.
 * You should use {@link BilinearMapImpl} for your implementation instead.
 */
public interface BilinearMap extends BiFunction<GroupElement, GroupElement, GroupElement> {
    /**
     * Returns the source group G1 associated with this bilinear map.
     */
    Group getG1();

    /**
     * Returns the source group G2 associated with this bilinear map.
     */
    Group getG2();

    /**
     * Returns the target group GT associated with this bilinear map.
     */
    Group getGT();

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
    GroupElement apply(GroupElement g1, GroupElement g2, BigInteger exponent);

    /**
     * Corresponds to {@code this.apply(g1, g2, 1)}.
     *
     * @param g1 left hand side argument for the pairing function
     * @param g2 right hand side argument for the pairing function
     */
    @Override
    default GroupElement apply(GroupElement g1, GroupElement g2) {
        return apply(g1, g2, BigInteger.ONE);
    }

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
    default GroupElement apply(GroupElement g1, GroupElement g2, Zn.ZnElement exponent) {
        return apply(g1, g2, exponent.getInteger());
    }

    /**
     * Applies the pairing function to each element of the given vectors, resulting in a new vector containing the
     * resulting target group elements.
     * The given vectors must have the same length for this method to work.
     * <p>
     * Specifically, the pairing function is applied to the elements residing at the same index.
     * For example, the first element of {@code lhs} and {@code rhs} will be used as argument, and the result
     * will be stored as the first element of the result vector. This continues until done for each element.
     *
     * @param lhs {@link GroupElementVector} containing the group elements to use a left hand side arguments
     * @param rhs {@code GroupElementVector} containing the group elements to use a right hand side arguments
     * @return a new {@code GroupElementVector} containing the resulting target group elements
     * @throws IllegalArgumentException if the given vectors do not have the same length
     */
    default GroupElementVector apply(GroupElementVector lhs, GroupElementVector rhs) {
        return new GroupElementVector(lhs.zip(rhs, this));
    }

    /**
     * Corresponds to first calling {@link #apply(GroupElementVector, GroupElementVector)} and then combining
     * the resulting elements using the group operation.
     * The given vectors must have the same length for this method to work.
     *
     * @param lhs {@link GroupElementVector} containing the group elements to use a left hand side arguments
     * @param rhs {@code GroupElementVector} containing the group elements to use a right hand side arguments
     * @return the resulting group element
     * @throws IllegalArgumentException if the given vectors do not have the same length
     */
    default GroupElement innerProduct(GroupElementVector lhs, GroupElementVector rhs) {
        return lhs.innerProduct(rhs, this);
    }

    /**
     * Creates a {@link PairingExpr} using this pairing and given argument {@link GroupElementExpression}s.
     * @param g1elem the left hand side G1 group element expression argument for the pairing function
     * @param g2elem the right hand side G2 group element expression argument for the pairing function
     * @return a {@code PairingExpr}
     */
    default PairingExpr applyExpr(GroupElementExpression g1elem, GroupElementExpression g2elem) {
        return new PairingExpr(this, g1elem, g2elem);
    }

    /**
     * Creates a {@link PairingExpr} using this pairing and given argument group elements.
     * @param g1elem the left hand side G1 element argument for the pairing function
     * @param g2elem the right hand side G2 element argument for the pairing function
     * @return a {@code PairingExpr}
     */
    default PairingExpr applyExpr(GroupElement g1elem, GroupElement g2elem) {
        return applyExpr(g1elem.expr(), g2elem.expr());
    }

    default PairingExpr applyExpr(GroupElement g1elem, GroupElementExpression g2elem) {
        return applyExpr(g1elem.expr(), g2elem);
    }

    default PairingExpr applyExpr(GroupElementExpression g1elem, GroupElement g2elem) {
        return applyExpr(g1elem, g2elem.expr());
    }

    /**
     * Returns true if {@code e(g,h).equals(e(h,g))} for all {@code g}, {@code h}.
     */
    boolean isSymmetric();
}
