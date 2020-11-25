package de.upb.crypto.math.interfaces.mappings;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.PairingExpr;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.StandaloneRepresentable;
import de.upb.crypto.math.structures.cartesian.GroupElementVector;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.math.BigInteger;
import java.util.function.BiFunction;

/**
 * A bilinear map \(e : \mathbb{G}_1 \times \mathbb{G}_2 \rightarrow \mathbb{G}_T\).
 * <p>
 * Bilinearity means that the map \(e\) is linear in both the first and second component.
 * \(\mathbb{G}_1, \mathbb{G}_2\) and \(\mathbb{G}_T\) are groups.
 */
public interface BilinearMap extends BiFunction<GroupElement, GroupElement, GroupElement> {
    Group getG1();
    Group getG2();
    Group getGT();

    /**
     * Computes {@code apply(g1,g2)^exponent}.
     * <p>
     * Depending on the bilinear map and the involved groups, this may be more efficiently implemented than computing
     * it directly via {@code apply(g1,g2).pow(exponent)}.
     * For example, implementations should do exponentiation in the group with the cheapest group operation.
     */
    GroupElement apply(GroupElement g1, GroupElement g2, BigInteger exponent);

    @Override
    default GroupElement apply(GroupElement g1, GroupElement g2) {
        return apply(g1, g2, BigInteger.ONE);
    }

    /**
     * Computes {@code apply(g1,g2)^exponent}.
     * <p>
     * Depending on the bilinear map and the involved groups, this may be more efficiently implemented than computing
     * it directly via {@code apply(g1,g2).pow(exponent)}.
     * For example, implementations should do exponentiation in the group with the cheapest group operation.
     */
    default GroupElement apply(GroupElement g1, GroupElement g2, ZnElement exponent) {
        return apply(g1, g2, exponent.getInteger());
    }

    default GroupElementVector apply(GroupElementVector lhs, GroupElementVector rhs) {
        return new GroupElementVector(lhs.zip(rhs, this));
    }

    default GroupElement innerProduct(GroupElementVector lhs, GroupElementVector rhs) {
        return lhs.innerProduct(rhs, this);
    }

    default PairingExpr expr(GroupElementExpression g1elem, GroupElementExpression g2elem) {
        return new PairingExpr(this, g1elem, g2elem);
    }

    default PairingExpr expr(GroupElement g1elem, GroupElement g2elem) {
        return expr(g1elem.expr(), g2elem.expr());
    }

    /**
     * Returns true if {@code e(g,h).equals(e(h,g))} for all {@code g}, {@code h}.
     */
    boolean isSymmetric();
}
