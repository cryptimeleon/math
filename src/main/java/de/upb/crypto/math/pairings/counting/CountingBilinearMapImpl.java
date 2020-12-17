package de.upb.crypto.math.pairings.counting;

import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.pairings.generic.BilinearMapImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A bilinear map (Zn,+) x (Zn,+) -> (Zn,+),
 * namely (a,b) -> a*b (multiplication in Zn).
 */
public class CountingBilinearMapImpl implements BilinearMapImpl {
    protected CountingGroupImpl g1, g2, gt;
    protected Zn zn;
    protected BigInteger size;
    protected BilinearGroup.Type pairingType;

    protected long numPairings;

    /**
     * Instantiates a debug bilinear map emulating pairing type "type"
     *
     * @param type type of the pairing (type 1: G1 = G2; type 2: G1 != G2 and there is a nondegenerate homomorphism G2 -> G1; type 3: G1 != G2 and there are no efficiently computable injective homomorphisms between G1 and G2
     * @param groupSize size of g1, g2, and gt (number of group elements)
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

    protected void incrementNumPairings() {
        ++numPairings;
    }
}
