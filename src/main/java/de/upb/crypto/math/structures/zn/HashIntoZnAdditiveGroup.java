package de.upb.crypto.math.structures.zn;

import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.structures.RingAdditiveGroup;
import de.upb.crypto.math.interfaces.structures.RingAdditiveGroup.RingAdditiveGroupElement;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

/**
 * Hashes into the additive subgroup of Zn.
 *
 * @see HashIntoZn
 */
public class HashIntoZnAdditiveGroup implements HashIntoStructure {
    protected HashIntoZn znHash;

    /**
     * target group
     */
    protected RingAdditiveGroup structure;

    /**
     * Corresponds to new HashIntoZn()
     */
    public HashIntoZnAdditiveGroup(BigInteger n) {
        this(new HashIntoZn(n));
    }

    /**
     * Corresponds to new HashIntoZn(String)
     */
    public HashIntoZnAdditiveGroup(Zn ring) {
        znHash = new HashIntoZn(ring.n);
        structure = new RingAdditiveGroup(znHash.getTargetStructure());
    }

    /**
     * Recreate hash function from representation
     */
    public HashIntoZnAdditiveGroup(Representation repr) {
        znHash = new HashIntoZn(repr);
        structure = new RingAdditiveGroup(znHash.getTargetStructure());
    }

    public HashIntoZnAdditiveGroup(HashIntoZn hashIntoZn) {
        znHash = hashIntoZn;
        structure = new RingAdditiveGroup(znHash.getTargetStructure());
    }

    @Override
    public RingAdditiveGroupElement hashIntoStructure(byte[] x) {
        return structure.new RingAdditiveGroupElement(znHash.hashIntoStructure(x));
    }

    @Override
    public Representation getRepresentation() {
        return znHash.getRepresentation();
    }


    @Override
    public int hashCode() {
        return structure.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HashIntoZnAdditiveGroup &&
                //structure.equals(((HashIntoZnAdditiveGroup) obj).structure) &&
                znHash.equals(((HashIntoZnAdditiveGroup) obj).znHash);
    }
}
