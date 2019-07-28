package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.BigIntegerRepresentation;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * A bilinear map (Zn,+) x (Zn,+) -> (Zn,+),
 * namely (a,b) -> a*b (multiplication in Zn).
 */
public class DebugBilinearMap implements BilinearMap {
    protected DebugGroup g1, g2, gt;
    protected Zn zn;
    protected BigInteger size;
    protected int pairingType;

    /**
     * Instantiates a debug bilinear map emulating pairing type "type"
     *
     * @param type type of the pairing (type 1: G1 = G2; type 2: G1 != G2 and there is a nondegenerate homomorphism G2 -> G1; type 3: G1 != G2 and there are no efficiently computable injective homomorphisms between G1 and G2
     * @param size size of g1, g2, and gt
     */
    public DebugBilinearMap(int type, BigInteger size) {
        this.size = size;
        this.zn = new Zn(size);
        this.pairingType = type;
        g1 = new DebugGroup("G1", size);
        if (type == 1)
            g2 = g1;
        else
            g2 = new DebugGroup("G2", size);
        gt = new DebugGroup("GT", size);
    }

    public DebugBilinearMap(Representation r) {
        this(r.obj().get("type").bigInt().getInt(), r.obj().get("size").bigInt().get());
    }

    @Override
    public Group getG1() {
        return g1;
    }

    @Override
    public Group getG2() {
        return g2;
    }

    @Override
    public Group getGT() {
        return gt;
    }

    @Override
    public GroupElement apply(GroupElement g1, GroupElement g2, BigInteger exponent) {
        return apply(g1.pow(exponent), g2);
    }

    @Override
    public GroupElement apply(GroupElement g1, GroupElement g2) {
        if (!(g1 instanceof DebugGroupElement) || !((DebugGroupElement) g1).group.equals(this.g1))
            throw new IllegalArgumentException("first pairing argument is not in " + this.g1.name + ". It's in "
                    + (!(g1 instanceof DebugGroupElement) ? g1.getStructure() : g1 == null ? null : ((DebugGroupElement) g1).group.name));
        if (!(g2 instanceof DebugGroupElement) || !((DebugGroupElement) g2).group.equals(this.g2))
            throw new IllegalArgumentException("first pairing argument is not in " + this.g2.name + ". It's in "
                    + (!(g2 instanceof DebugGroupElement) ? g2.getStructure() : g2 == null ? null : ((DebugGroupElement) g2).group.name));

        DebugGroupLogger.log("e", "pairing");
        return gt.wrap(((DebugGroupElement) g1).elem.mul(((DebugGroupElement) g2).elem));
    }

    @Override
    public String toString() {
        return "DebugPairing";
    }

    @Override
    public boolean isSymmetric() {
        return pairingType == 1;
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation repr = new ObjectRepresentation();
        repr.put("type", new BigIntegerRepresentation(pairingType));
        repr.put("size", new BigIntegerRepresentation(size));

        return repr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugBilinearMap that = (DebugBilinearMap) o;
        return pairingType == that.pairingType &&
                Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, pairingType);
    }
}
