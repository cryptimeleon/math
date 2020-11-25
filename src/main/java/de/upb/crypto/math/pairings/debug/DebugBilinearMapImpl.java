package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.mappings.impl.BilinearMapImpl;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.BigIntegerRepresentation;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A bilinear map \(e : (\mathbb{Z}_n,+) \times (\mathbb{Z}_n,+) \rightarrow (\mathbb{Z}_n,+),
 * namely \((a,b) \mapsto a \cdot b \pmod n\), i.e. multiplication in \(\mathbb{Z}_n\).
 */
public class DebugBilinearMapImpl implements BilinearMapImpl {
    protected DebugGroupImpl g1, g2, gt;
    protected Zn zn;
    protected BigInteger size;
    protected BilinearGroup.Type pairingType;

    private long numPairings;

    /**
     * Instantiates a debug bilinear map emulating the given pairing type.
     *
     * @param type type of the pairing
     * @param groupSize size of \(\mathbb{G}_1\), \(\mathbb{G}_2\), and \(\mathbb{G}_T\) (number of group elements)
     * @param enableExpCounting whether to enable counting of exponentiations performed in each group
     * @param enableMultiExpCounting whether to enable counting of multi-exponentiations performed in each group
     */
    public DebugBilinearMapImpl(BilinearGroup.Type type, BigInteger groupSize, boolean enableExpCounting,
                                boolean enableMultiExpCounting) {
        this.size = groupSize;
        this.zn = new Zn(groupSize);
        this.pairingType = type;
        g1 = new DebugGroupImpl("G1", groupSize, enableExpCounting, enableMultiExpCounting);
        if (type == BilinearGroup.Type.TYPE_1)
            g2 = g1;
        else
            g2 = new DebugGroupImpl("G2", groupSize, enableExpCounting, enableMultiExpCounting);
        gt = new DebugGroupImpl("GT", groupSize, enableExpCounting, enableMultiExpCounting);
        numPairings = 0;
    }

    @Override
    public GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2, BigInteger exponent) {
        return apply(g1.pow(exponent), g2);
    }

    @Override
    public GroupElementImpl apply(GroupElementImpl g1, GroupElementImpl g2) {
        if (g1 == null) {
            throw new IllegalArgumentException("First pairing argument is null");
        } else if (!(g1 instanceof DebugGroupElementImpl)) {
            throw new IllegalArgumentException("First pairing argument is not a DebugGroupElementImpl instance");
        } else if (!((DebugGroupElementImpl) g1).group.equals(this.g1)) {
            throw new IllegalArgumentException("First pairing argument is not in group " + this.g1.name + ". "
                    + "It's in group " + ((DebugGroupElementImpl) g1).group.name);
        }
        if (g2 == null) {
            throw new IllegalArgumentException("Second pairing argument is null");
        } else if (!(g2 instanceof DebugGroupElementImpl)) {
            throw new IllegalArgumentException("Second pairing argument is not a DebugGroupElementImpl instance");
        } else if (!((DebugGroupElementImpl) g2).group.equals(this.g2)) {
            throw new IllegalArgumentException("Second pairing argument is not in group " + this.g2.name + ". "
                    + "It's in group " + ((DebugGroupElementImpl) g2).group.name);
        }

        GroupElementImpl result = gt.wrap(((DebugGroupElementImpl) g1).elem.mul(((DebugGroupElementImpl) g2).elem));
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
        DebugBilinearMapImpl that = (DebugBilinearMapImpl) o;
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

    private void incrementNumPairings() {
        ++numPairings;
    }
}
