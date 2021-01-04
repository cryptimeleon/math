package de.upb.crypto.math.pairings.counting;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.pairings.generic.BilinearMapImpl;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A {@link BilinearMapImpl} implementing a fast, but insecure pairing over {@link Zn}.
 * Allows for counting pairings.
 * <p>
 * The bilinear map works by mapping {@code (Zn,+) x (Zn,+)} to {@code (Zn,+)} via {@code (a,b) -> a*b}
 * (multiplication in Zn).
 * It is insecure since DLOG is trivial in Zn.
 */
public class CountingBilinearMapImpl implements BilinearMapImpl {
    /**
     * The groups underlying the bilinear group.
     */
    protected CountingGroupImpl g1, g2, gt;

    /**
     * Zn of order the bilinear group's size.
     */
    protected Zn zn;

    /**
     * The order of the bilinear group.
     */
    protected BigInteger size;

    /**
     * The type of pairing this bilinear map offers.
     */
    protected BilinearGroup.Type pairingType;

    /**
     * The counted number of pairings.
     */
    protected long numPairings;

    /**
     * Instantiates this bilinear map with the given pairing type, group size, and counting configuration.
     *
     * @param type type of the pairing
     * @param groupSize size of g1, g2, and gt (number of group elements)
     * @param enableExpCounting if {@code true}, exponentiations in G1, G2 and GT are counted as a single unit
     *                          and group operations within exponentiations are not counted; otherwise the former is
     *                          not done and group operations within exponentiations are added to the total count
     * @param enableMultiExpCounting if {@code true}, number of terms in each multi-exponentiation is tracked and
     *                               group operations within multi-exponentiations are not counted; otherwise
     *                               the former is not done and group operations within multi-exponentiations
     *                               are added to the total count
     */
    public CountingBilinearMapImpl(BilinearGroup.Type type, BigInteger groupSize, boolean enableExpCounting,
                                   boolean enableMultiExpCounting) {
        this.size = groupSize;
        this.zn = new Zn(groupSize);
        this.pairingType = type;
        g1 = new CountingGroupImpl("G1", groupSize, enableExpCounting, enableMultiExpCounting);
        if (type == BilinearGroup.Type.TYPE_1)
            g2 = g1;
        else
            g2 = new CountingGroupImpl("G2", groupSize, enableExpCounting, enableMultiExpCounting);
        gt = new CountingGroupImpl("GT", groupSize, enableExpCounting, enableMultiExpCounting);
        numPairings = 0;
    }

    @Override
    public GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2, BigInteger exponent) {
        return apply(g1.pow(exponent), g2);
    }

    @Override
    public GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2) {
        if (!(g1 instanceof CountingGroupElementImpl) || !((CountingGroupElementImpl) g1).group.equals(this.g1))
            throw new IllegalArgumentException("first pairing argument is not in " + this.g1.name + ". It's in "
                    + (!(g1 instanceof CountingGroupElementImpl) ? g1.getStructure() : g1 == null ? null : ((CountingGroupElementImpl) g1).group.name));
        if (!(g2 instanceof CountingGroupElementImpl) || !((CountingGroupElementImpl) g2).group.equals(this.g2))
            throw new IllegalArgumentException("first pairing argument is not in " + this.g2.name + ". It's in "
                    + (!(g2 instanceof CountingGroupElementImpl) ? g2.getStructure() : g2 == null ? null : ((CountingGroupElementImpl) g2).group.name));

        GroupElementImpl result = gt.wrap(((CountingGroupElementImpl) g1).elem.mul(((CountingGroupElementImpl) g2).elem));
        incrementNumPairings();
        return result;
    }

    @Override
    public String toString() {
        return "DebugPairing";
    }

    @Override
    public boolean isSymmetric() {
        return pairingType == BilinearGroup.Type.TYPE_1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountingBilinearMapImpl that = (CountingBilinearMapImpl) o;
        return pairingType == that.pairingType &&
                Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, pairingType);
    }

    /**
     * Retrieves number of pairings computed in this bilinear group.
     */
    public long getNumPairings() {
        return numPairings;
    }

    /**
     * Resets pairing counter.
     */
    public void resetNumPairings() {
        numPairings = 0;
    }

    /**
     * Increments the pairing counter.
     */
    protected void incrementNumPairings() {
        ++numPairings;
    }
}
