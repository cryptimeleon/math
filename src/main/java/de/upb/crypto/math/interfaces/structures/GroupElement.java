package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.expressions.group.GroupElementConstantExpr;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory.GroupPrecomputations;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.math.BigInteger;
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
     * @return this element "squared" (if op is an multiplication), or "doubled" (if op is an addition)
     * If there is a more efficient algorithm for squaring (e.g. for elliptic curve points),
     * these classes should override this method.
     */
    default GroupElement square() {
        return this.op(this);
    }

    /**
     * Calculates the result of applying the group operation k times.
     * i.e. it computes k*this (additive group) or this^k (multiplicative group).
     * For negative exponents k, computes this.inv().pow(-k).
     *
     * Implementations wanting to override pow() behavior should override this method instead of the ZnELement and long overloads.
     */
    default GroupElement powBinSquareMultiply(BigInteger k) { //default implementation: square&multiply algorithm
        if (k.signum() < 0)
            return powBinSquareMultiply(k.negate()).inv();
        GroupElement operand = this;

        GroupElement result = getStructure().getNeutralElement();
        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = result.op(result);
            if (k.testBit(i))
                result = result.op(operand);
        }
        return result;
    }

    default GroupElement pow(BigInteger exponent) {
        return this.pow(exponent, false);
    }

    /**
     * Optimized implementation that automatically chooses correct algorithm.
     * @param exponent
     */
    default GroupElement pow(BigInteger exponent, boolean enableCaching) {
        if (this.getStructure().estimateCostOfInvert() <= 100) {
            // use signed digit WNAF method
            if (enableCaching) {
                return this.powWnaf(exponent, 8, enableCaching);
            } else {
                return this.powWnaf(exponent, 4, enableCaching);
            }
        } else {
            if (enableCaching) {
                return this.powSlidingWindow(exponent, 8, enableCaching);
            } else {
                return this.powSlidingWindow(exponent, 4, enableCaching);
            }
        }
    }

    default GroupElement powSlidingWindow(BigInteger exponent) {
        // Probably cannot assume that caching will be worth it by default
        return powSlidingWindow(exponent, 4, false);
    }

    default GroupElement powSlidingWindow(BigInteger exponent, boolean enableCaching) {
        if (enableCaching) {
            return powSlidingWindow(exponent, 8, true);
        } else {
            return powSlidingWindow(exponent, 4, false);
        }
    }

    default GroupElement powSlidingWindow(BigInteger exponent, int windowSize,
                                          boolean enableCaching) {
        if (exponent.signum() < 0)
            return powSlidingWindow(exponent.negate(), windowSize, enableCaching).inv();
        List<GroupElement> smallOddPowersOfBase;
        int oddPowersMaxExp = (1<<windowSize)-1;
        if (enableCaching) {
            GroupPrecomputations groupPrecomputations =
                    GroupPrecomputationsFactory.get(this.getStructure());
            smallOddPowersOfBase = groupPrecomputations.getOddPowers(this, oddPowersMaxExp);
        } else {
            smallOddPowersOfBase = UncachedGroupPrecomputations
                    .precomputeSmallOddPowers(this, oddPowersMaxExp);
        }
        GroupElement y = this.getStructure().getNeutralElement();
        int l = exponent.bitLength();
        int i = l - 1;
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
                y = y.square();
                i--;
            }
        }
        return y;
    }

    /**
     * @return base^exponent in the group, using the wNAF approach
     */
    default GroupElement powWnaf(BigInteger exponent, int windowSize, boolean enableCaching) {
        if (exponent.signum() < 0)
            return powWnaf(exponent.negate(), windowSize, enableCaching).inv();
        int oddPowersMaxExp = (1<<windowSize)-1;
        int[] exponentDigits;
        List<GroupElement> smallOddPowers;
        if (enableCaching) {
            GroupPrecomputations groupPrecomputations =
                    GroupPrecomputationsFactory.get(this.getStructure());
            smallOddPowers = groupPrecomputations.getOddPowers(this, oddPowersMaxExp);
        } else {
            smallOddPowers = UncachedGroupPrecomputations
                    .precomputeSmallOddPowers(this, oddPowersMaxExp);
        }
        exponentDigits = UncachedGroupPrecomputations
                .precomputeExponentDigitsForWnaf(exponent, windowSize);

        GroupElement A = this.getStructure().getNeutralElement();
        for (int i = exponentDigits.length-1; i >= 0; i--) {
            if (i != exponentDigits.length-1) {
                A = A.square();
            }
            int exponentDigit = exponentDigits[i];
            if (exponentDigit != 0) {
                GroupElement power = smallOddPowers.get(Math.abs(exponentDigit) / 2);
                if (exponentDigit < 0) {
                    power = power.inv();
                }
                A = A.op(power);
            }
        }
        return A;
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
