package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.expressions.group.GroupElementConstantExpr;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.raphael.GroupPrecomputationsFactory;
import de.upb.crypto.math.raphael.GroupPrecomputationsFactory.GroupPrecomputations;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Immutable objects representing elements of a group.
 * <p>
 * Implementations must properly implement equals() and hashCode()
 */
public interface GroupElement extends Element, UniqueByteRepresentable {
    @Override
    public Group getStructure();

    /**
     * Calculates the inverse of this group element
     *
     * @return an element x such that x.op(this).equals(getStructure().getNeutralElement())
     */
    GroupElement inv();

    /**
     * Calculates the result of e op this.
     *
     * @param e right hand side of the operation
     * @return the element resulting from the group operation
     * @throws IllegalArgumentException if e is of the wrong type
     */
    GroupElement op(Element e) throws IllegalArgumentException;

    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes this.inv().pow(-k).
     *
     * Implementations wanting to override pow() behavior should override this method instead of the ZnELement and long overloads.
     */
    default GroupElement pow(BigInteger k) { //default implementation: square&multiply algorithm
        if (k.signum() < 0)
            return pow(k.negate()).inv();
        GroupElement operand = this;

        GroupElement result = getStructure().getNeutralElement();
        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = result.op(result);
            if (k.testBit(i))
                result = result.op(operand);
        }
        return result;
    }

    default List<GroupElement> precomputeSmallOddPowers(int maxExp) {
        List<GroupElement> res = new ArrayList<>();
        res.add(this.getStructure().getNeutralElement());
        for (int i = 1; i < (maxExp+1)/2; i++) {
            res.add(res.get(i-1).op(this));
        }
        return res;
    }

    default GroupElement powSlidingWindow(BigInteger exponent, int windowSize,
                                          boolean enableCaching) {
        List<GroupElement> smallOddPowersOfBase;
        int oddPowersMaxExp = (1<<windowSize)-1;
        if (enableCaching) {
            GroupPrecomputations groupPrecomputations =
                    GroupPrecomputationsFactory.get(this.getStructure());
            smallOddPowersOfBase = groupPrecomputations.getOddPowers(this, oddPowersMaxExp);
        } else {
            smallOddPowersOfBase = this.precomputeSmallOddPowers(oddPowersMaxExp);
        }
        GroupElement y = this.getStructure().getNeutralElement();
        int l = exponent.bitLength();
        int i = l - 1;
        if (windowSize > 20) {
            throw new IllegalArgumentException("too large windowSize");
        }
        while (i > -1) {
            if (exponent.testBit(i)) {
                int s = Math.max(0, i - windowSize + 1);
                int smallExponent = 0;
                while (!exponent.testBit(s)) {
                    s++;
                }
                for (int h = s; h <= i; h++) {
                    y = y.op(y);
                    if (exponent.testBit(h)) {
                        smallExponent += 1 << h - s;
                    }
                }

                y = y.op(smallOddPowersOfBase.get(smallExponent / 2));
                i = s - 1;
            } else {
                y = y.op(y);
                i--;
            }
        }
        return y;
    }

    /**
     * Calculates the result of applying the group operation k times.
     * Note that this is only well-defined if k is from Zn, such that getStructure().size() divides n.
     */
    default GroupElement pow(ZnElement k) {
        return pow(k.getInteger());
    }

    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes this.inv().pow(-k).
     * <p>
     * The caller should be aware that usually, exponents for large groups will not usually
     * fit into a long value (use pow(BigInteger) or pow(ZnElement) if your exponent is large).
     */
    default GroupElement pow(long k) {
        return pow(BigInteger.valueOf(k));
    }

    /**
     * Returns true iff this is the neutral element of the group.
     */
    default boolean isNeutralElement() {
        return this.equals(getStructure().getNeutralElement());
    }

    /**
     * Returns a {@link de.upb.crypto.math.expressions.group.GroupElementExpression} containing exactly this group element.
     */
    default GroupElementConstantExpr expr() {
        return new GroupElementConstantExpr(this);
    }
}
